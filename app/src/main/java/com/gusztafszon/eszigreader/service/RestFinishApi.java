package com.gusztafszon.eszigreader.service;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public class RestFinishApi implements Callable<Response> {

    private static final String SEND_DOC_NO = "ca/verify/finish";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String idServerPath;
    private String uid;


    public  RestFinishApi(String idServerPath, String uid){
        this.idServerPath = idServerPath;
        this.uid = uid;
    }


    @Override
    public Response call() throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", uid);
        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url(idServerPath + "/" + SEND_DOC_NO)
                .post(body)
                .build();


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
        //OkHttpClient client = new OkHttpClient();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }
}
