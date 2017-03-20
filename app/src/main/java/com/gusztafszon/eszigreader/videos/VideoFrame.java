package com.gusztafszon.eszigreader.videos;

import java.io.ByteArrayInputStream;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class VideoFrame {
    private byte[] data;
    private byte[] processedData;

    public VideoFrame(byte[] data) {
        this.data = data;
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
}
