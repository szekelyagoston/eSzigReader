package com.gusztafszon.eszigreader.service.dto;

/**
 * Created by Gusztafszon on 2017-04-10.
 */

public class RegistrationDto {

    private byte[] stream;
    private String path;
    private String docId;
    private String username;

    public RegistrationDto(byte[] stream, String path, String docId, String username) {
        this.stream = stream;
        this.path = path;
        this.docId = docId;
        this.username = username;
    }

    public byte[] getStream() {
        return stream;
    }

    public void setStream(byte[] stream) {
        this.stream = stream;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
