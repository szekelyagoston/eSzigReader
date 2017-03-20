package com.gusztafszon.eszigreader.utils;

/**
 * Created by agoston.szekely on 2016.10.21..
 */

public enum CountDownType {

    SECONDS(1000), TENTH_OF_SEC(100);

    private Integer howManyMillisecToMatch;

    CountDownType(Integer howManyMillisecToMatch){
        this.howManyMillisecToMatch = howManyMillisecToMatch;
    }

    public Integer getHowManyMillisecToMatch() {
        return howManyMillisecToMatch;
    }
}
