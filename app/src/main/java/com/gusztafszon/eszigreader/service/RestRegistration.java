package com.gusztafszon.eszigreader.service;

import com.gusztafszon.eszigreader.service.dto.RegistrationDto;

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

    private RegistrationDto registrationDto;

    public RestRegistration(RegistrationDto registrationDto) {
        this.registrationDto = registrationDto;
    }

    @Override
    public Response call() throws Exception {


        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("card", registrationDto.getDocId())
                .addFormDataPart("username", registrationDto.getUsername())
                .addFormDataPart("image", "image", RequestBody.create(MEDIA_TYPE_MARKDOWN, registrationDto.getStream()))
                .build();

        Request request = new Request.Builder()
                .url(registrationDto.getPath() + "/" + REGISTER_PATH)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        return response;
    }

}
