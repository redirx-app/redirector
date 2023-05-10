package app.rdrx.directory.model.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RecordFieldErrors extends RdrxException{

    private final Map<String, List<String>> fieldErrors;
    private final List<String> nonFieldErrors;
    private final Short statusCode;

    public RecordFieldErrors(){
        super();
        this.fieldErrors = new HashMap<>();
        this.nonFieldErrors = new ArrayList<>();
        this.statusCode = null;
    }

    public RecordFieldErrors(short statusCode){
        super();
        this.fieldErrors = new HashMap<>();
        this.nonFieldErrors = new ArrayList<>();
        this.statusCode = statusCode;
    }

    public void throwIfHasErrors() throws RecordFieldErrors{
        if(
            (this.fieldErrors != null && !this.fieldErrors.isEmpty()) || 
            (this.nonFieldErrors != null && !this.nonFieldErrors.isEmpty())
        ) {
            throw this;
        }
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
    
    protected void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors.clear();
        for(Entry<String, List<String>> errors : fieldErrors.entrySet()){
            this.fieldErrors.computeIfAbsent(errors.getKey(), k -> new ArrayList<>()).addAll(errors.getValue());
        }
    }

    public void addFieldError(String field, String error){
        if(field == null || error == null){
            throw new IllegalArgumentException("field and error cannot be null.");
        }
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
    }
    
    public List<String> getNonFieldErrors() {
        return nonFieldErrors;
    }
    
    protected void setNonFieldErrors(List<String> nonFieldErrors) {
        this.nonFieldErrors.clear();
        this.nonFieldErrors.addAll(nonFieldErrors);
    }
    
    public void addNonFieldError(String error){
        nonFieldErrors.add(error);
    }

    public Short getStatusCode() {
        return statusCode;
    }

    public void addErrors(RecordFieldErrors other){
        if(other.getFieldErrors() != null && !other.getFieldErrors().isEmpty()){
            if(this.fieldErrors.isEmpty()){
                this.setNonFieldErrors(other.nonFieldErrors);
            } else {
                for(Entry<String, List<String>> fieldError : other.getFieldErrors().entrySet()){
                    this.fieldErrors.computeIfAbsent(fieldError.getKey(), k -> new ArrayList<String>()).addAll(fieldError.getValue());
                }
            }
        }
        
        if(other.getNonFieldErrors() != null && !other.getNonFieldErrors().isEmpty()){
            if(this.nonFieldErrors.isEmpty()){
                this.setNonFieldErrors(other.getNonFieldErrors());
            } else {
                this.nonFieldErrors.addAll(other.getNonFieldErrors());
            }
        }
    }

}
