package com.gusztafszon.eszigreader.utils;

import android.graphics.Matrix;

import java.io.IOException;

/**
 * Created by agoston.szekely on 2016.10.14..
 */

public class OrientationHelper {
    //http://stackoverflow.com/questions/7286714/android-get-orientation-of-a-camera-bitmap-and-rotate-back-90-degrees
    //Not working the original way. There is a bug with android, exif is not working. Rotating back 90 degrees -> always user selfies
    public static final Matrix rotate270() throws IOException {

        int rotationInDegrees = 270;
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationInDegrees);

        return matrix;
    }

}
