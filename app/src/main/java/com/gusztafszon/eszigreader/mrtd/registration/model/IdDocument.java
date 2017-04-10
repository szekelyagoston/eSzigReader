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

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFormattedExpirationDate(){
        return formatToDateFormat(expirationDate);
    }

    public String getFormattedDateOfBirth(){
        return formatToDateFormat(dateOfBirth);
    }

    private String formatToDateFormat(String string) {
        if (string == null){
            return null;
        }
        if (string.length() == 6){
            StringBuilder builder = new StringBuilder();
            builder.append(string.substring(0, 2));
            builder.append(("/"));
            builder.append(string.substring(2, 4));
            builder.append(("/"));
            builder.append(string.substring(4, 6));

            return builder.toString();
        }

        return "";

    }

    public boolean isValid() {
        return dateOfBirth != null && dateOfBirth != "" && expirationDate != null && expirationDate != "" && documentNumber != null && documentNumber != "" ;
    }
}
