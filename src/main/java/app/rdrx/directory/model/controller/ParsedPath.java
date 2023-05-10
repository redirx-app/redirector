package app.rdrx.directory.model.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedPath{
    private String alias = null;
    private List<String> args = null;
    private Map<String, List<String>> kwargs = null;

    public ParsedPath(){
        kwargs = new HashMap<>();
        args = new ArrayList<>();
    }

    public ParsedPath(String[] parsedAliasAndArgs, Map<String, List<String>> parsedKwargs){
        if(parsedAliasAndArgs != null){
            switch(parsedAliasAndArgs.length){
                case 0: 
                    this.setAlias(null);
                    this.setArgs(new ArrayList<>());
                    break;
                case 1:
                    this.setAlias(parsedAliasAndArgs[0]);
                    this.setArgs(new ArrayList<>());
                    break;
                default:
                    this.setAlias( parsedAliasAndArgs[0] );
                    this.setArgs(new ArrayList<>( Arrays.asList(parsedAliasAndArgs) ));
                    this.args.remove(0);
                    break;
            }
        } else {
            this.setAlias(null);
            this.setArgs(new ArrayList<>());
        }
        this.kwargs = parsedKwargs;
    }

    public ParsedPath(String alias, String[] parsedArgs, Map<String, List<String>> parsedKwargs){
        this.setAlias(alias);
        this.setArgs( new ArrayList<>( Arrays.asList(parsedArgs) ) );
        this.setKwargs(parsedKwargs);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public Map<String, List<String>> getKwargs() {
        return kwargs;
    }

    public void setKwargs(Map<String, List<String>> kwargs) {
        this.kwargs = kwargs;
    }

    
}
