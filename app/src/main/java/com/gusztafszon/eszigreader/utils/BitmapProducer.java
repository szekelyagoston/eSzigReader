package com.gusztafszon.eszigreader.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by agoston.szekely on 2016.10.14..
 */

public class BitmapProducer {
    public static final Bitmap CreateBitmap(Bitmap src, Matrix rotationMatrix){
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), rotationMatrix, true);
    }

    public static final Bitmap MirrorBitmap(Bitmap src){
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.preScale(-1, +1);
        src = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), rotationMatrix, true);
        return src;
    }


}
