package app.rdrx.directory.model.controller;

import java.util.List;
import java.util.Map;

import app.rdrx.directory.model.exceptions.RecordFieldErrors;

public class RecordErrorResponse {
    Map<String, List<String>> fieldErrors;
    List<String> nonFieldErrors;
    String message;
    String status;

    public RecordErrorResponse(RecordFieldErrors recordErrors){
        this.fieldErrors = recordErrors.getFieldErrors();
        this.nonFieldErrors = recordErrors.getNonFieldErrors();
        this.message = recordErrors.getMessage();
        this.status = "error";
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<String> getNonFieldErrors() {
        return nonFieldErrors;
    }

    public void setNonFieldErrors(List<String> nonFieldErrors) {
        this.nonFieldErrors = nonFieldErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
}
