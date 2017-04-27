package com.gusztafszon.eszigreader.videos;

import java.io.ByteArrayInputStream;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class VideoFrame {
    private byte[] data;
    private byte[] processedData;
    private int sorrend;
    private long processedMilliSec;

    public VideoFrame(byte[] data, int i) {
        this.data = data;
        this.sorrend = i;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getProcessedData() {
        return processedData;
    }

    public void setProcessedData(byte[] processedData) {
        this.processedData = processedData;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSorrend() {
        return sorrend;
    }

    public void setSorrend(int sorrend) {
        this.sorrend = sorrend;
    }

    public long getProcessedMilliSec() {
        return processedMilliSec;
    }

    public void setProcessedMilliSec(long processedMilliSec) {
        this.processedMilliSec = processedMilliSec;
    }
}
