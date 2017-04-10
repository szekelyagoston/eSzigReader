package com.gusztafszon.eszigreader.service.dto;

/**
 * Created by Gusztafszon on 2017-04-10.
 */

public class ResultDto {
    private Boolean success;

    private String message;

    public ResultDto(Boolean success) {
        this.success = success;
        this.message = "";
    }

    public ResultDto(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
