package com.gusztafszon.eszigreader.tasks;

import android.os.AsyncTask;

import com.gusztafszon.eszigreader.service.dto.RegistrationDto;

import okhttp3.Response;

/**
 * Created by Gusztafszon on 2017-04-10.
 */

public class ChallengeTask extends AsyncTask<RegistrationDto, Void, Response> {
    @Override
    protected Response doInBackground(RegistrationDto... params) {
        return null;
    }
}
