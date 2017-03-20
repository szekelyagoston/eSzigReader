package com.gusztafszon.eszigreader.utils;

import android.os.CountDownTimer;

/**
 * Created by agoston.szekely on 2016.10.21..
 */
//HACKED Beacouse CountDownTimer does not call ontTick when remaining millis is smaller than interval. *10 and / 10 is a hack to "solve" it. It really does not solve it, buit the difference will be 10 times smaller
public class SecondCountDownTimer {
    private Integer secondsRemaining;
    private Integer tick;
    private ICountDownEvents delegate;
    private CountDownType type;

    public SecondCountDownTimer(Integer seconds, Integer tick, CountDownType type, ICountDownEvents delegate){
        this.secondsRemaining = seconds * 10;
        this.delegate = delegate;
        this.tick = tick;
        this.type = type;
    }

    public void start(){

        //type.getHowManyMillisecToMatch() / 10 -> divide by 10, and run onTick for every tenth click. This is the hack
        new CountDownTimer(secondsRemaining * type.getHowManyMillisecToMatch() / 10, tick * (type.getHowManyMillisecToMatch() / 10)) {
            public void onTick(long millisUntilFinished) {
                secondsRemaining = secondsRemaining - tick;
                if (secondsRemaining % 10 == 0){
                    delegate.onTick(secondsRemaining / 10);
                }
            }

            public void onFinish() {
                delegate.onFinish();
            }
        }.start();
    }
}
