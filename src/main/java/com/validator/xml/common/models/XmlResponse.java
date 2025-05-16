package com.validator.xml.common.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XmlResponse {
    private boolean isValid;
    private List<String> errors;

    public XmlResponse() {
        this.isValid = true;
        this.errors = new ArrayList<>();
    }

    public void addError(String error) {
        this.isValid = false;
        this.errors.add(error);
    }

    // Getters and Setters
    public boolean getIsValid() { return isValid; }
    public List<String> getErrors() { return errors; }
    public void setValid(boolean isValid) { this.isValid = isValid; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
