package com.gusztafszon.eszigreader.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class RestVideoApi implements Callable<Response> {

    private static final String SEND_PICTURE_PATH = "ca/verify/picture";
    private static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("image/jpeg");

    private byte[] stream;
    private String idServerPath;
    private String uid;
    private int compressLevel;
    private int sorrend;
    private long processedMilliSec;

    public RestVideoApi(String idServerPath, String uid, byte[] stream, int sorrend, int compressLevel, long processedMilliSec) {
        this.stream = stream;
        this.idServerPath = idServerPath;
        this.uid = uid;
        this.sorrend = sorrend;
        this.compressLevel = compressLevel;
        this.processedMilliSec = processedMilliSec;
    }

    @Override
    public Response call() throws Exception {


        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", uid)
                .addFormDataPart("sorrend", String.valueOf(sorrend))
                .addFormDataPart("compressLevel", String.valueOf(compressLevel))
                .addFormDataPart("processedMilliSec", String.valueOf(processedMilliSec))
                .addFormDataPart("image", "image", RequestBody.create(MEDIA_TYPE_MARKDOWN, stream))
                .build();

        Request request = new Request.Builder()
                .url(idServerPath + "/" + SEND_PICTURE_PATH)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
        Response response = client.newCall(request).execute();

        return response;
    }
}
