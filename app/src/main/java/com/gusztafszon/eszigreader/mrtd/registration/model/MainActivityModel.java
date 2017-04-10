package com.gusztafszon.eszigreader.mrtd.registration.model;

import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gusztafszon on 2016.12.22..
 */

public class MainActivityModel {
    private IdDocument document;

    private String idServerPath;

    private String uid;

    private String type;

    private String url;

    private String userName;

    private boolean nfcEnabled;

    public IdDocument getDocument() {
        return document;
    }

    public void setDocument(IdDocument document) {
        this.document = document;
    }

    public void setDocument(MRTDRegistrationDto document) {
        this.document = new IdDocument(document);
    }

    public String getIdServerPath() {
        return idServerPath;
    }

    public void setIdServerPath(String idServerPath) {
        this.idServerPath = idServerPath;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isNfcEnabled() {
        return nfcEnabled;
    }

    public void setNfcEnabled(boolean nfcEnabled) {
        this.nfcEnabled = nfcEnabled;
    }

    public boolean isLoginValid(){
        return idServerPath != null && idServerPath != "" && uid != null && uid != "";
    }

    public boolean isRegistrationValid(){
        return url != null && url != "" && userName != null && userName != "";
    }
}
