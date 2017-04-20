package com.gusztafszon.eszigreader.callbacks;

import com.gusztafszon.eszigreader.service.dto.ResultDto;

/**
 * Created by Gusztafszon on 2017-04-10.
 */

public interface IChallengeCallback {
    //ezt kell átírni hogy message is menjen vissza!
    void onResult(ResultDto success);
}
