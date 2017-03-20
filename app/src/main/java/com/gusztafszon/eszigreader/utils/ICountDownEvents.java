package com.gusztafszon.eszigreader.utils;

/**
 * Created by agoston.szekely on 2016.10.21..
 */

public interface ICountDownEvents {
    void onTick(Integer secondsRemaining);
    void onFinish();
}
