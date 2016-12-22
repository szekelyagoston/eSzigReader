package com.gusztafszon.eszigreader.mrtd.registration.model;

import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;

/**
 * Created by gusztafszon on 2016.12.22..
 */

public class IdDocument {

    private String documentNumber;
    private String expirationDate;
    private String dateOfBirth;

    public IdDocument(MRTDRegistrationDto dto) {
        this.documentNumber = dto.getDocumentNumber();
        this.expirationDate = dto.getExpirationDate();
        this.dateOfBirth = dto.getDateOfBirth();
    }

    public IdDocument(String documentNumber, String expirationDate, String dateOfBirth) {
        this.documentNumber = documentNumber;
        this.expirationDate = expirationDate;
        this.dateOfBirth = dateOfBirth;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
