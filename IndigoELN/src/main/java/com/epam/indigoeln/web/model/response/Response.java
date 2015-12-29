package com.epam.indigoeln.web.model.response;

public class Response {

    private Boolean success;
    private String message;
    private Object data;

    public static Response success() {
        Response response = new Response();
        response.setSuccess(true);
        return response;
    }

    public static Response failure() {
        Response response = new Response();
        response.setSuccess(false);
        return response;
    }

    public Response withMessage(String message) {
        this.message = message;
        return this;
    }

    public Response withData(Object data) {
        this.data = data;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
