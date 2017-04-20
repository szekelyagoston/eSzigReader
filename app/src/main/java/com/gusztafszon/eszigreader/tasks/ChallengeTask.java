package com.gusztafszon.eszigreader.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.gusztafszon.eszigreader.activities.NfcActivity;
import com.gusztafszon.eszigreader.callbacks.IChallengeCallback;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;
import com.gusztafszon.eszigreader.service.RestFinishApi;
import com.gusztafszon.eszigreader.service.RestVideoApi;
import com.gusztafszon.eszigreader.service.dto.RegistrationDto;
import com.gusztafszon.eszigreader.service.dto.ResultDto;
import com.gusztafszon.eszigreader.videos.IVideoAnalyzer;
import com.gusztafszon.eszigreader.videos.VideoFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class ChallengeTask extends AsyncTask<Void, Void, Void> {

    private IChallengeCallback cb;

    private ProgressDialog dialog;

    private MainActivityModel model;

    private IVideoAnalyzer videoAnalyzer;

    public ChallengeTask(NfcActivity activity, MainActivityModel model, IVideoAnalyzer videoAnalyzer, IChallengeCallback cb){
        this.cb = cb;
        this.dialog = new ProgressDialog(activity);
        this.model = model;
        this.videoAnalyzer = videoAnalyzer;
    }

    @Override
    protected Void doInBackground(Void... params) {

        List<VideoFrame> frames = videoAnalyzer.filterFrames();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(frames.size());
        List<Response> responses = new ArrayList<Response>();
        for (VideoFrame frame : frames){
            Callable<Response> callable =  new RestVideoApi(model.getIdServerPath(), model.getUid(), frame.getProcessedData());
            Future<Response> future = executor.schedule(callable, 0, TimeUnit.MILLISECONDS);
            Response result = null;
            try {
                result= future.get();
                responses.add(result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        Callable<Response> callable =  new RestFinishApi(model.getIdServerPath(), model.getUid());
        Future<Response> future = executor.schedule(callable, 0, TimeUnit.MILLISECONDS);
        Response result = null;
        try {
            result = future.get(100, TimeUnit.SECONDS);
            cb.onResult(new ResultDto(result.isSuccessful(), result.body().string()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            cb.onResult(new ResultDto(false, "There was a problem, please try again later!"));

        } catch (ExecutionException e) {
            e.printStackTrace();
            cb.onResult(new ResultDto(false, "There was a problem, please try again later!"));

        } catch (IOException e) {
            e.printStackTrace();
            cb.onResult(new ResultDto(false, "There was a problem, please try again later!"));

        }catch(Exception e){
            e.printStackTrace();
            cb.onResult(new ResultDto(false, "There was a problem, please try again later!"));
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Performing challenge verification");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
