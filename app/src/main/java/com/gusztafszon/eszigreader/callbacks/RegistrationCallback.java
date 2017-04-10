package com.gusztafszon.eszigreader.callbacks;

import okhttp3.Response;

/**
 * Created by Gusztafszon on 2017-04-10.
 */

public interface RegistrationCallback {
    void onFinish(Response result);
}
