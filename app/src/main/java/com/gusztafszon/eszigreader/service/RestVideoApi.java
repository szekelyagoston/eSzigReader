package com.gusztafszon.eszigreader.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

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

    public RestVideoApi(String idServerPath, String uid, byte[] stream) {
        this.stream = stream;
        this.idServerPath = idServerPath;
        this.uid = uid;
    }

    @Override
    public Response call() throws Exception {


        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", uid)
                .addFormDataPart("image", "image", RequestBody.create(MEDIA_TYPE_MARKDOWN, stream))
                .build();

        Request request = new Request.Builder()
                .url(idServerPath + "/" + SEND_PICTURE_PATH)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        return response;
    }
}
