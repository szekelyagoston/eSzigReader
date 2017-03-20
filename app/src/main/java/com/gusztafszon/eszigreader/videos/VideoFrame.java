package com.gusztafszon.eszigreader.videos;

import java.io.ByteArrayInputStream;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class VideoFrame {
    private byte[] data;
    private ByteArrayInputStream processedInputStream;

    public VideoFrame(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public ByteArrayInputStream getProcessedInputStream() {
        return processedInputStream;
    }

    public void setProcessedInputStream(ByteArrayInputStream processedInputStream) {
        this.processedInputStream = processedInputStream;
    }
}
