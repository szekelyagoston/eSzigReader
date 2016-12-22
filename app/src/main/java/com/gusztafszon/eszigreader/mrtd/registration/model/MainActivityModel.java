package com.gusztafszon.eszigreader.mrtd.registration.model;

import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gusztafszon on 2016.12.22..
 */

public class MainActivityModel {
    private IdDocument document;

    public IdDocument getDocument() {
        return document;
    }

    public void setDocument(IdDocument document) {
        this.document = document;
    }

    public void setDocument(MRTDRegistrationDto document) {
        this.document = new IdDocument(document);
    }
}
