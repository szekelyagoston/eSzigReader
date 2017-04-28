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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class VideoAnalyzer implements IVideoAnalyzer{

    private static final int MAX_SELECTED_FRAMES = 15;

    private List<VideoFrame> frames = new ArrayList<>();

    private static List<VideoFrame> processedframes = new ArrayList<>(MAX_SELECTED_FRAMES);

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

        // a potentially  time consuming task
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
        float currentValue = 0f;

        ExecutorService service = Executors.newFixedThreadPool(MAX_SELECTED_FRAMES);
        List<Future<Runnable>> futures = new ArrayList<Future<Runnable>>();

        for (int i = 0; i < resultCount; ++i)
        {
            currentValue = currentValue + steps;
            int nextIndex = ((int) currentValue) - 1;

            final VideoFrame frame = frames.get(nextIndex);
            System.out.println("SIZE BEFORE ANYTHING: ******** (INDEX "+nextIndex+"): "+ frame.getData().length / 1024 + " KB");



            Future f = service.submit( new Thread(new Runnable() {

                public void run() {
                    int compressLevel = findingCompressLevel(frame.getData().length);
                    //calculating inputstream
                    YuvImage yuv = new YuvImage(frame.getData(), parameters.getPreviewFormat(), width, height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(0, 0, width, height), compressLevel, out);

                    byte[] bytes = out.toByteArray();
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    bitmap.recycle();
                    //Bitmap comperssed = BitmapFactory.decodeStream(new ByteArrayInputStream(outputStream.toByteArray()));
                    //frame.setProcessedData(outputStream.toByteArray());
                    frame.setProcessedData(outputStream.toByteArray());
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    processedframes.add(frame);
                    System.out.println("SIZE AFTER : ******** (INDEX ): "+ frame.getProcessedData().length / 1024 + " KB");
                }

            })
            );

            futures.add(f);
        }
        // wait for all tasks to complete before continuing
        for (Future<Runnable> f : futures)
        {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //shut down the executor service so that this thread can exit
        service.shutdownNow();
        return processedframes;

    }


    private int findingCompressLevel(int length) {
        if ((length / 1024) > 1000){
            //for my LG phone
            return 30;
        }else{
            //fro Szilvika phone
            return 100;
        }
    }


    @Override
    public void resetFrames() {
        this.processedframes.clear();
    }
}
