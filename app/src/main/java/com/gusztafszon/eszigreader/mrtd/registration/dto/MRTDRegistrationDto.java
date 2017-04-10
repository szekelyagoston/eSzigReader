package com.gusztafszon.eszigreader.mrtd.registration.dto;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gusztafszon on 2016.11.30..
 */

public class MRTDRegistrationDto {
    private String documentNumber;
    private String expirationDate;
    private String dateOfBirth;

    public MRTDRegistrationDto(String documentNumber, Date expDate, Date dateOfBirth) {
        this.documentNumber = documentNumber;
        this.expirationDate = formatDateToString(expDate);
        this.dateOfBirth = formatDateToString(dateOfBirth);
    }

    private String formatDateToString(Date dateFormat) {
        if (dateFormat == null){
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateFormat);
        StringBuilder builder = new StringBuilder();
        builder.append(Integer.toString(cal.get(Calendar.YEAR)).substring(2,4));

        //counting from 0.
        String month = Integer.toString(cal.get(Calendar.MONTH)  + 1 );
        //If month not 10, 11, 12 then an extra 0 should be included
        if (month.length() < 2) {
            builder.append("0");
        }
        builder.append(month);

        String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        if (day.length() < 2) {
            builder.append("0");
        }
        builder.append(day);

        return builder.toString();
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
}
