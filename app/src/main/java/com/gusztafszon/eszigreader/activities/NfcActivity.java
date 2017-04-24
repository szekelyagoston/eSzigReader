package com.gusztafszon.eszigreader.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gusztafszon.eszigreader.R;
import com.gusztafszon.eszigreader.callbacks.BACCallback;
import com.gusztafszon.eszigreader.callbacks.IChallengeCallback;
import com.gusztafszon.eszigreader.constants.Constants;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;
import com.gusztafszon.eszigreader.service.RestFinishApi;
import com.gusztafszon.eszigreader.service.RestVideoApi;
import com.gusztafszon.eszigreader.service.camera.CameraPreview;
import com.gusztafszon.eszigreader.service.dto.ResultDto;
import com.gusztafszon.eszigreader.tasks.BACTask;
import com.gusztafszon.eszigreader.tasks.ChallengeTask;
import com.gusztafszon.eszigreader.utils.CountDownType;
import com.gusztafszon.eszigreader.utils.ICountDownEvents;
import com.gusztafszon.eszigreader.utils.SecondCountDownTimer;
import com.gusztafszon.eszigreader.videos.IVideoAnalyzer;
import com.gusztafszon.eszigreader.videos.VideoAnalyzer;
import com.gusztafszon.eszigreader.videos.VideoFrame;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
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
 * Created by agoston.szekely on 2017.01.05..
 */

public class NfcActivity  extends AppCompatActivity {

    private MainActivityModel model = new MainActivityModel();

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.getProviders();
    }

    private final static Integer COUNTDOWNSECONDS = 3;
    private final static Integer COUNTDOWNINTERVAL = 1;
    //means 2/10 -> 2sec
    private final static Integer TIMEOFCHALLENGE = 20;
    //Means 1/10 sec ->callback will be in every 0,1sec --> that way we will send 20-1(due to the strange countdown bug) frames
    private final static Integer CHALLENGEFRAMECOUNT = 1;
    private static final String ERROR_COLOR="#FF0000";
    private static final String SUCCESS_COLOR="#009900";

    private Camera camera;
    private CameraPreview cameraPreview;

    private TextView textView;
    private SecondCountDownTimer timer;
    private Button button;

    private SharedPreferences preferences;
    private Boolean analyzeRunning = false;

    private IVideoAnalyzer videoAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getting shared preferences
        preferences = getSharedPreferences(Constants.MY_REFERENCES, 0);
        setCurrentDocumentFromPreferences();

        setContentView(R.layout.nfcreader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView)findViewById(R.id.text_challenge);

        //checking whether nfc started this activity
        if (getIntent() == null || getIntent().getExtras() == null) {
            return;
        }
        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        button = (Button)findViewById(R.id.button_challenge);
        button.setEnabled(false);

        if (model.getType().equals("R")){
            button.setVisibility(View.GONE);
        }


        if (model.isLoginValid() || model.isRegistrationValid()){
            doNfc(tag);
        }else{
            textView.setText("Error! App should be launched only from a webpage!");
            textView.setTextColor(Color.parseColor(ERROR_COLOR));
            textView.setTypeface(null, Typeface.BOLD);
        }

    }

    private void doNfc(Tag tag) {

        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            final PassportService ps = new PassportService(cs);
            ps.open();

            ps.sendSelectApplet(false);
            BACKeySpec bacKey = new BACKeySpec() {
                @Override
                public String getDocumentNumber() {
                    return model.getDocument().getDocumentNumber();
                }

                @Override
                public String getDateOfBirth() {
                    return model.getDocument().getDateOfBirth();
                }

                @Override
                public String getDateOfExpiry() {
                    return model.getDocument().getExpirationDate();
                }
            };

            BACTask bacTask = new BACTask(ps, NfcActivity.this, model, new BACCallback() {
                @Override
                public void onFinish(final ResultDto dto) {
                    switch (model.getType()){
                        case "R": {
                            finishRegistration(dto);
                            break;
                        }
                        case "L" : {
                            NfcActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    finishLoginFirstStep(dto);
                                }
                            });

                            break;
                        }
                    }


                }
            });
            if (model.getDocument().isValid()){
                bacTask.execute(bacKey);
            }else{
                textView.setText("Error! Not every card data is provided!");
                textView.setTextColor(Color.parseColor(ERROR_COLOR));
                textView.setTypeface(null, Typeface.BOLD);
            }


        }catch (CardServiceException e){
            e.printStackTrace();
        }
        finally {

        }
    }

    private void finishLoginFirstStep(ResultDto dto) {
        if (dto.getSuccess()){
            startCamera();
            videoAnalyzer = new VideoAnalyzer(camera.getParameters());

            final String challengeMessage = dto.getMessage();

            button.setEnabled(true);

            textView.setText("First step successful! \n Click the button to start challenge!");
            textView.setTextColor(Color.parseColor(SUCCESS_COLOR));
            textView.setTypeface(null, Typeface.BOLD);

            button.setText("START CHALLENGE");

            button.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startCountDown(challengeMessage);
                            button.setEnabled(false);
                        }
                    }
            );
        }else{
            button.setEnabled(true);

            textView.setText(dto.getMessage());
            textView.setTextColor(Color.parseColor(ERROR_COLOR));
            textView.setTypeface(null, Typeface.BOLD);

            button.setText("BACK TO LOGIN");

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NfcActivity.this.finishAffinity();
                }
            });
        }
        if (videoAnalyzer != null){
            videoAnalyzer.resetFrames();
        }

    }

    private void finishRegistration(final ResultDto dto) {
        if (dto.getSuccess()){

            NfcActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    textView.setText("REGISTRATION SUCCESSFUL");
                    textView.setTextColor(Color.parseColor(SUCCESS_COLOR));
                    textView.setTypeface(null, Typeface.BOLD);

                    button.setText("BACK TO LOGIN");

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NfcActivity.this.finishAffinity();
                        }
                    });
                }
            });



        }else{
            NfcActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setVisibility(View.VISIBLE);
                    textView.setText(dto.getMessage());
                    textView.setTextColor(Color.parseColor(ERROR_COLOR));
                    textView.setTypeface(null, Typeface.BOLD);
                    button.setEnabled(true);
                    button.setText("BACK TO LOGIN");
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NfcActivity.this.finishAffinity();
                        }
                    });
                }
            });
        }
    }

    private void startCountDown(final String text) {

        textView.setText(text + " in " + Integer.toString(COUNTDOWNSECONDS) + " seconds");
        textView.setTextColor(Color.BLACK);
        startCountDownTimer(text);
    }

    private void startCountDownTimer(final String text) {
        setupCountDownTimer(text);

        timer.start();
    }

    private void setupCountDownTimer(final String text) {
        timer = new SecondCountDownTimer(COUNTDOWNSECONDS, COUNTDOWNINTERVAL, CountDownType.SECONDS, new ICountDownEvents() {

            @Override
            public void onTick(Integer secondsRemaining) {
                textView.setText(text + " in " + Integer.toString(secondsRemaining) + " seconds");
            }

            @Override
            public void onFinish() {
                startChallenge(text);
            }
        });
    }



    private void startChallenge(final String text) {
        analyzeRunning = true;

        textView.setText(text + " NOW!");

        new SecondCountDownTimer(TIMEOFCHALLENGE, CHALLENGEFRAMECOUNT,CountDownType.TENTH_OF_SEC, new ICountDownEvents() {

            @Override
            public void onTick(Integer secondsRemaining) {
                //Do nothing
            }

            @Override
            public void onFinish(){
                analyzeRunning = false;
                releaseCamera();


                ChallengeTask challengeTask = new ChallengeTask(NfcActivity.this, model, videoAnalyzer, new IChallengeCallback() {
                    @Override
                    public void onResult(final ResultDto result) {
                        if (result.getSuccess()){
                            NfcActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setEnabled(true);

                                    textView.setText("Login successful! \n Click the button to finish login!");
                                    textView.setTextColor(Color.parseColor(SUCCESS_COLOR));
                                    textView.setTypeface(null, Typeface.BOLD);

                                    button.setText("RETURN TO LOGIN PAGE");

                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            NfcActivity.this.finishAffinity();
                                        }
                                    });
                                }
                            });


                        }else{
                            NfcActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setEnabled(true);
                                    textView.setText("Login not successful! \n " + result.getMessage() + "\n Click the button to return the main page!");
                                    textView.setTextColor(Color.parseColor(ERROR_COLOR));
                                    textView.setTypeface(null, Typeface.BOLD);

                                    button.setText("RETURN TO LOGIN PAGE");
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            NfcActivity.this.finishAffinity();
                                        }
                                    });
                                }
                            });

                        }
                    }
                });

                challengeTask.execute();

            }


        }).start();

    }

    private void startCamera(){
        if (checkCameraHardware(getApplicationContext())){
            camera = getCameraInstance();
            if (camera != null){
                cameraPreview = new CameraPreview(this, camera, previewCallback);
                camera.setPreviewCallback(previewCallback);
                FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
                preview.addView(cameraPreview);
            }else{

                textView.setText("ERROR! Could not access to camera!");

            }

        }else{
               textView.setText("ERROR! Camera not found!");
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            // this device has a front camera
            return true;
        } else {
            // no front camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    /**should change to camer2 later, with @module and interface. First make it work*/
    @SuppressWarnings("deprecation")
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int cameraId = 0; cameraId < cameraCount; cameraId++){
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    try{
                        c = Camera.open(cameraId); // attempt to get a Camera instance
                    }catch(RuntimeException e){
                        e.printStackTrace();
                    }

                }
            }
        }
        catch (Exception e){
            textView.setText("ERROR! Front camera not available!");
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //camera.startPreview();
            if (analyzeRunning){
                videoAnalyzer.addFrame(new VideoFrame(data));
            }
        }

    };



    private void setCurrentDocumentFromPreferences() {
        model.setDocument(new IdDocument(preferences.getString(Constants.DOCUMENT_NUMBER, ""), preferences.getString(Constants.EXPIRATION_DATE, ""), preferences.getString(Constants.DATE_OF_BIRTH, "")));
        model.setIdServerPath(preferences.getString(Constants.APP_URL, ""));
        model.setUid(preferences.getString(Constants.UID, ""));
        model.setType(preferences.getString(Constants.TYPE, ""));
        model.setUrl(preferences.getString(Constants.URL, ""));
        model.setUserName(preferences.getString(Constants.USERNAME, ""));
    }

    private void releaseCamera(){
        if (cameraPreview!= null){
            cameraPreview.releaseCamera();
        }

        if (camera != null){
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
}
