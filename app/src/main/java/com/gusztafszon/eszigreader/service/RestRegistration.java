package com.gusztafszon.eszigreader.service;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Gusztafszon on 2017-03-21.
 */

public class RestRegistration implements Callable<Response> {

    private static final String REGISTER_PATH = "facelogin/api/public/register";
    private static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("image/jpeg");

    private byte[] stream;
    private String path;
    private String docId;
    private String username;

    public RestRegistration(String path, byte[] stream, String docId, String username) {
        this.stream = stream;
        this.path = path;
        this.docId = docId;
        this.username = username;
    }

    @Override
    public Response call() throws Exception {


        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("card", docId)
                .addFormDataPart("username", username)
                .addFormDataPart("image", "image", RequestBody.create(MEDIA_TYPE_MARKDOWN, stream))
                .build();

        Request request = new Request.Builder()
                .url(path + "/" + REGISTER_PATH)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        return response;
    }

}
