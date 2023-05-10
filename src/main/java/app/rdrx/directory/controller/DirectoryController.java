package app.rdrx.directory.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.rdrx.directory.model.controller.RecordErrorResponse;
import app.rdrx.directory.model.exceptions.DoesNotExistException;
import app.rdrx.directory.model.exceptions.RecordFieldErrors;
import app.rdrx.directory.model.service.Reference;
import app.rdrx.directory.model.service.ReferenceDetail;
import app.rdrx.directory.service.ReferenceService;

@RestController
@RequestMapping("/api")
public class DirectoryController {

    private static final short STATUS_NOT_FOUND = 404;

    private static final String REF_ID_FIELD = "id";

    @Autowired
    private ReferenceService refService;

    @ExceptionHandler({RecordFieldErrors.class})
    public RecordErrorResponse handleException(RecordFieldErrors errors, HttpServletResponse response){
        if(errors.getStatusCode() != null){
            response.setStatus(errors.getStatusCode());
        } else {
            response.setStatus(400); // Blanket 400 malformed response here, unless specified by the RecordFieldErrors.
        }
        RecordErrorResponse responseBody = new RecordErrorResponse(errors);
        responseBody.setStatus("Record Field Error");
        return responseBody;
    }

    @GetMapping("/ref")
    public Page<ReferenceDetail> referenceSearch(
        @RequestParam(name="q", required=false) String[] queryValues,
        @RequestParam(name="page", required=false) Integer page,
        @RequestParam(name="pageSize", required=false) Integer pageSize,
        @RequestParam(name="sort", required=false) String sort,
        @RequestParam(name="sortOrder", required=false) String sortOrder,
        HttpServletRequest request, HttpServletResponse response){
        List<String> queryParams = new ArrayList<>();
        if(queryValues != null){
            Arrays.asList(queryValues).forEach(value -> queryParams.addAll(Arrays.asList(value.split("\\s"))));
        }
        queryParams.removeIf(param -> param == null || param.isBlank());
        return refService.search(queryParams, pageSize, page, sort, sortOrder);
    }
    
    @GetMapping("/ref/{alias}")
    public ReferenceDetail getReferenceDetails(@PathVariable String alias, HttpServletResponse response) throws IOException, RecordFieldErrors{
        try{
            return refService.getByAliasOrId(alias).orElseThrow();
        } catch (NoSuchElementException ex) {
            response.sendError(STATUS_NOT_FOUND, ex.getMessage());
        }
        return null;
    }

    @PostMapping("/ref")
    public Reference createNewReference(@RequestBody Reference ref, HttpServletResponse response) throws RecordFieldErrors {
        try{
            return refService.createReference(ref);
        } catch (NoSuchElementException ex) {
            response.setStatus(201);
            return null;
        }
    }

    @RequestMapping(path = "/ref/{refId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public Reference updateReference(@PathVariable String refId, @RequestBody Reference ref, HttpServletRequest request, HttpServletResponse response) throws RecordFieldErrors, IOException {
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        if(refId.isBlank()){
            recordErrors.addFieldError(REF_ID_FIELD, "cannot be null id");
        }
        if (ref == null){
            recordErrors.addNonFieldError("Reference cannot be null");
        } else if(ref.getId() == null){
            ref.setId(refId);
        } else if (!ref.getId().equals(refId)){
            recordErrors.addFieldError(REF_ID_FIELD, "Id / Record Mismatch");
        }
        recordErrors.throwIfHasErrors();
        try{
            return refService.updateReference(
                ref, 
                request.getMethod().equalsIgnoreCase(RequestMethod.PATCH.toString())
            );
        } catch (DoesNotExistException ex){
            response.sendError(STATUS_NOT_FOUND, "Reference Not Found");
        }
        return null;
    }

    @DeleteMapping("/ref/{refId}")
    public void deleteReferenceById(@PathVariable String refId, HttpServletResponse response) throws IOException{
        if(refId.isBlank()){
            response.sendError(404);
        }
        refService.deleteReference(refId);
    }
}
