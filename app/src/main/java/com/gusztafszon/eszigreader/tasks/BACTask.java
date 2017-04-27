package com.gusztafszon.eszigreader.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;

import com.gusztafszon.eszigreader.activities.NfcActivity;
import com.gusztafszon.eszigreader.callbacks.BACCallback;
import com.gusztafszon.eszigreader.callbacks.RegistrationCallback;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;
import com.gusztafszon.eszigreader.service.RestApi;
import com.gusztafszon.eszigreader.service.RestRegistration;
import com.gusztafszon.eszigreader.service.dto.RegistrationDto;
import com.gusztafszon.eszigreader.service.dto.ResultDto;
import com.gusztafszon.eszigreader.videos.VideoAnalyzer;

import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.LDSFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;

/**
 * Created by Gusztafszon on 2017-04-10.
 */

public class BACTask extends AsyncTask<BACKeySpec, Void, ResultDto>{

    private PassportService ps;

    private ProgressDialog dialog;

    private BACCallback callback;

    private MainActivityModel model;

    private NfcActivity activity;

    public BACTask(PassportService ps, NfcActivity activity,  MainActivityModel model, BACCallback callback){
        this.ps = ps;
        this.dialog = new ProgressDialog(activity);
        this.callback = callback;
        this.model = model;
        this.activity = activity;
    }

    @Override
    protected ResultDto doInBackground(BACKeySpec... params) {
        try {
            ps.doBAC(params[0]);

        } catch (CardServiceException e) {
            callback.onFinish(new ResultDto(false, "Error! Could not authenticate card!"));
            return null;
        }

        switch(model.getType()){
            case "R": {
                //REGISTRATION
                Response result = doRegistration(ps);
                if (result == null){
                    callback.onFinish(new ResultDto(false, "Error! Could not authenticate card!"));
                }
                if (result != null && result.isSuccessful()){
                    callback.onFinish(new ResultDto(true));
                }
                break;
            }

            case "L": {
                //LOGIN
                doLogin(ps);
                break;
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Doing Basic Access Control authentication against ID card");
        dialog.show();
    }

    @Override
    protected void onPostExecute(ResultDto result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private Response doRegistration(PassportService ps) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage("Performing registration");
            }
        });


        InputStream dg1InputStream = null;
        Response result= null;
        try{
            InputStream pictureInputStream = null;
            pictureInputStream = ps.getInputStream(PassportService.EF_DG2);
            DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, pictureInputStream);

            Bitmap bmp = BitmapFactory.decodeStream(dg2.getFaceInfos().get(0).getFaceImageInfos().get(0).getImageInputStream());
            if (bmp == null){
                callback.onFinish(new ResultDto(false, "Registration was not successful!"));
                return result;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //compress quality -> if too high, method will be slow.
            bmp.compress(Bitmap.CompressFormat.JPEG, 3, outputStream);

            byte[] mypic = outputStream.toByteArray();

            dg1InputStream = ps.getInputStream(PassportService.EF_DG1);
            DG1File dg1 = (DG1File)LDSFileUtil.getLDSFile(PassportService.EF_DG1, dg1InputStream);

            String docId = dg1.getMRZInfo().getDocumentNumber();


            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Callable<Response> callable =  new RestRegistration(new RegistrationDto( mypic, model.getUrl(), docId, model.getUserName()));
            Future<Response> future = executor.schedule(callable, 0, TimeUnit.MILLISECONDS);

            try {
                result= future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                callback.onFinish(new ResultDto(false, "Registration was not successful!"));
            } catch (ExecutionException e) {
                e.printStackTrace();
                callback.onFinish(new ResultDto(false, "Registration was not successful!"));
            }finally {
                executor.shutdown();
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if (dg1InputStream != null){
                    dg1InputStream.close();
                }

            }catch(Exception e){
                e.printStackTrace();
            }

        }
        if (!result.isSuccessful()){
            try {
                callback.onFinish(new ResultDto(false, result.body().string()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void doLogin(PassportService ps) {
        InputStream dg1InputStream = null;
        try{

            dg1InputStream = ps.getInputStream(PassportService.EF_DG1);
            DG1File dg1 = (DG1File)LDSFileUtil.getLDSFile(PassportService.EF_DG1, dg1InputStream);

            String docId = dg1.getMRZInfo().getDocumentNumber();

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Callable<Response> callable =  new RestApi(docId, model.getIdServerPath(), model.getUid());
            Future<Response> future = executor.schedule(callable, 0, TimeUnit.MILLISECONDS);
            Response result= future.get();

            callback.onFinish(new ResultDto(result.isSuccessful(), result.body().string()));

            executor.shutdown();
        }catch (Exception e){
            new ResultDto(false, "Unknown error happened!");
            e.printStackTrace();
        }finally {
            try{
                if (dg1InputStream != null){
                    dg1InputStream.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
