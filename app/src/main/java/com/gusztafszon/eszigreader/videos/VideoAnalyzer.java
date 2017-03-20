package com.gusztafszon.eszigreader.videos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import com.gusztafszon.eszigreader.utils.BitmapProducer;
import com.gusztafszon.eszigreader.utils.OrientationHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class VideoAnalyzer implements IVideoAnalyzer{

    private static final int MAX_SELECTED_FRAMES = 20;

    private List<VideoFrame> frames = new ArrayList<>();

    private Camera.Parameters parameters;
    private int width;
    private int height;

    public VideoAnalyzer(Camera.Parameters parameters) {
        this.parameters = parameters;
        this.height = parameters.getPreviewSize().height;
        this.width = parameters.getPreviewSize().width;
    }

    @Override
    public void addFrame(VideoFrame videoFrame) {
        this.frames.add(videoFrame);
    }

    @Override
    public List<VideoFrame> getFrames() {
        return frames;
    }

    @Override
    public List<VideoFrame> filterFrames() {
        //maximum MAX_SELECTED_FRAMES frames should be selected
        float steps;
        int resultCount;
        //we have less than MAX_SELECTED_FRAMES frames to choose from.
        if (frames.size() < MAX_SELECTED_FRAMES){
            steps = 1;
            resultCount = frames.size();
        }else{
            //we have more than MAX_SELECTED_FRAMES frames, so we choose MAX_SELECTED_FRAMES.
            steps = frames.size() / MAX_SELECTED_FRAMES;
            resultCount = MAX_SELECTED_FRAMES;
        }
        List<VideoFrame> framesForProcessing = new ArrayList<>(MAX_SELECTED_FRAMES);
        float currentValue = 0f;
        for (int i = 0; i < resultCount; ++i){
            currentValue = currentValue + steps;
            int nextIndex = ((int)currentValue) - 1;

            VideoFrame frame = frames.get(nextIndex);
            //calculating inputstream
            YuvImage yuv = new YuvImage(frame.getData(), parameters.getPreviewFormat(), width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

            byte[] bytes = out.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix rotationMatrix = null;
            try {
                rotationMatrix = OrientationHelper.rotate270();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap = BitmapProducer.CreateBitmap(bitmap, rotationMatrix);
            bitmap = BitmapProducer.MirrorBitmap(bitmap);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //compress quality -> if too high, method will be slow.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 3, outputStream);
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(outputStream.toByteArray());

            frame.setProcessedInputStream(inputStream);
            framesForProcessing.add(frame);
        }

        return framesForProcessing;
    }
}
