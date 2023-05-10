package app.rdrx.directory.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import app.rdrx.directory.model.controller.ParsedPath;
import app.rdrx.directory.model.exceptions.RecordFieldErrors;
import app.rdrx.directory.model.exceptions.UrlBuildException;
import app.rdrx.directory.model.exceptions.UrlParseException;
import app.rdrx.directory.model.service.PathSlice;
import app.rdrx.directory.model.service.Reference;
import app.rdrx.directory.model.service.ReferenceParam;
import app.rdrx.directory.service.ReferenceService;

@Controller
public class RedirectController {

    private Logger logger = LoggerFactory.getLogger(RedirectController.class);

    @Autowired
    private ReferenceService refService;

    private static final String LOCATION = "Location";
    private static final String APP_NEW_REF = "http://go/app/new";
    private static final int STATUS_REDIRECT = 302;
    private static final int STATUS_SERVER_ERROR = 500;

    @GetMapping("/new")
    public void appRedirect(HttpServletResponse response){
        response.setHeader(LOCATION, APP_NEW_REF);
        response.setStatus(STATUS_REDIRECT);
    }

    @GetMapping(value = {
        "/{path:(?!(?:new|app|api|favicon\\.ico)$).*}",
        "/{path:(?!(?:new|app|api|favicon\\.ico)$).*}/**",
    })
    public void redirectResponse(HttpServletRequest request, HttpServletResponse response) throws IOException{
        ParsedPath ppath = null;
        try{
            ppath = new ParsedPath(
                parsePathVariables(request.getRequestURI()),
                parseParams(request.getQueryString())
            );
            
            Reference ref = refService.getByAlias(ppath.getAlias(), true).orElseThrow();
            String href = evaluatedReference(ppath, ref);
            response.setHeader(LOCATION, href);
            
        } catch(UrlParseException ex) {
            // Failed to parse path. Most likely there is no actual link.
            response.setHeader(LOCATION, APP_NEW_REF);
        } catch (NoSuchElementException ex) {
            // This reference doesn't exist yet. redirect the user to create a new 
            String redirectLocation = APP_NEW_REF;
            if(ppath != null){
                redirectLocation += "?alias=" + ppath.getAlias();
            }
            response.setHeader(LOCATION, redirectLocation);
            
        } catch (UrlBuildException ex) {
            response.sendError(STATUS_SERVER_ERROR, "There was an issue building the reference.");
        } catch (RecordFieldErrors err){
            response.sendError(STATUS_SERVER_ERROR, "There was an issue requesting the link");
        }
        logger.info(
            "Redirecting: {} to {}",  
            request.getRequestURL(), 
            response.getHeader(LOCATION)
        );
        response.setStatus(STATUS_REDIRECT);
    }

    private String[] parsePathVariables(String path) throws UrlParseException{
        String[] parsedPathItems = path.split("[/\\\\]"); // expecting URI: /<alias>. does not include hostname.
        if(parsedPathItems.length < 2){ 
            throw new UrlParseException("Could not find full path");
        }
        parsedPathItems = Arrays.copyOfRange(parsedPathItems, 1, parsedPathItems.length);
        return parsedPathItems;
        
    }

    private Map<String, List<String>> parseParams(String params) throws UrlParseException{
        Map<String, List<String>> result = new HashMap<>();
        if(params != null && !params.isBlank()){
            String[] paramSplit = params.split("&");
            for(String param : paramSplit){
                if(!param.isEmpty() && !param.isBlank()){
                    if(param.contains("=")){
                        String[] kv = param.split("=", 2);
                        if(!result.containsKey(kv[0])){
                            result.put(kv[0], new ArrayList<>());
                        }
                        result.get(kv[0]).add(kv[1]);
                    } else {
                        throw new UrlParseException("Poorly formatted query String.");
                    }
                }
            }
        }
        return result;
    }

    private String evaluateParam(String name, ParsedPath userParams, List<ReferenceParam> defaults) throws UrlBuildException{
        if(name == null){
            throw new UrlBuildException("Cannot evaluate a null-named param.");
        }
        int paramIndex = -1;
        ReferenceParam paramDefault = null;
        // first, try to evaluate from query parameters
        if(userParams.getKwargs().containsKey(name) && !userParams.getKwargs().get(name).isEmpty()){
            return userParams.getKwargs().get(name).remove(0);
        } else {
        // second, find the default in the list of params
            for(int i = 0; i < defaults.size(); i++){
                ReferenceParam d = defaults.get(i);
                if(d.getName().equalsIgnoreCase(name)){
                    paramIndex = i;
                    paramDefault = d;
                    break;
                }
            }
            // error out if we don't find a default.
            if(paramIndex == -1 || paramDefault == null){
                throw new UrlBuildException("Param name mismatch: " + name);
            } else {
                // using the position of the default, if there is an arg in that position, use that value.
                if(userParams.getArgs().size() > paramIndex){
                    return userParams.getArgs().get(paramIndex);
                } else {
                // fallback to the default.
                    return paramDefault.getDefaultValue();
                }
            }
        }
    }

    private String evaluatedReference(ParsedPath params, Reference ref) throws UrlBuildException{
        List<PathSlice> slices = ref.getUrlSlices();
        List<ReferenceParam> defaults = ref.getParams();
        StringBuilder hrefBuilder = new StringBuilder();
        Set<String> usedParams = new HashSet<>();
        for(PathSlice slice : slices){
            switch(slice.getType()){
                case "text":
                    hrefBuilder.append(slice.getValue());
                    break;
                case "param":
                    hrefBuilder.append(evaluateParam(slice.getValue(), params, defaults));
                    usedParams.add(slice.getValue());
                    break;
                default:
                    throw new UrlBuildException("Unexpected slice type: " + slice.getType());
            }
        }
        List<String> extraKwargs = new ArrayList<>();
        for(Entry<String, List<String>> extra : params.getKwargs().entrySet()){

            for(String value : extra.getValue()){
                extraKwargs.add(extra.getKey() + "=" + value);
            }
        }
        if(!extraKwargs.isEmpty()){
            if(hrefBuilder.toString().contains("?")){
                hrefBuilder.append("&");
            } else {
                hrefBuilder.append("?");
            }
            hrefBuilder.append(String.join("&", extraKwargs));
        }
        return hrefBuilder.toString();
    }
}