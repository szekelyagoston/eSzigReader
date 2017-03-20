package com.gusztafszon.eszigreader.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.gusztafszon.eszigreader.R;
import com.gusztafszon.eszigreader.constants.Constants;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;
import com.gusztafszon.eszigreader.service.RestApi;
import com.gusztafszon.eszigreader.service.camera.CameraPreview;
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
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.LDSFileUtil;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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

    private Camera camera;
    private CameraPreview cameraPreview;

    private TextView textView;
    private SecondCountDownTimer timer;

    private SharedPreferences preferences;
    private ImageView imageView;
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

        //checking whether nfc started this activity
        if (getIntent() == null || getIntent().getExtras() == null) {
            return;
        }
        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        Button button = (Button)findViewById(R.id.button_challenge);
        button.setEnabled(false);
        button.setText("WAITING FOR NFC");

        doNfc(tag);
    }

    private void doNfc(Tag tag) {
        PassportService ps = null;

        try {
            IsoDep nfc = IsoDep.get(tag);
            CardService cs = CardService.getInstance(nfc);
            ps = new PassportService(cs);
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

            ps.doBAC(bacKey);
            //InputStream pictureInputStream = null;
            InputStream dg1InputStream = null;
            try{
                dg1InputStream = ps.getInputStream(PassportService.EF_DG1);
                DG1File dg1 = (DG1File)LDSFileUtil.getLDSFile(PassportService.EF_DG1, dg1InputStream);

                String docId = dg1.getMRZInfo().getDocumentNumber();

                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                Callable<Response> callable =  new RestApi(docId, model.getIdServerPath(), model.getUid());
                Future<Response> future = executor.schedule(callable, 0, TimeUnit.MILLISECONDS);
                Response result= future.get();

                if (result.isSuccessful()){
                    startCamera();
                    //TODO: refactor maybe to a new file, this is ugly as hell
                    videoAnalyzer = new VideoAnalyzer(camera.getParameters());

                    textView = (TextView)findViewById(R.id.text_challenge);
                    final String challengeMessage = result.body().string();

                    Button button = (Button)findViewById(R.id.button_challenge);
                    button.setEnabled(true);
                   //button.setText("CLOSE APP AND REDIRECT TO LOGIN");
                    button.setText("START CHALLENGE");

                    button.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startCountDown(challengeMessage);
                                }
                            }
                    );



                    /*button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NfcActivity.this.finishAffinity();
                        }
                    });*/
                }

                executor.shutdown();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    dg1InputStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }catch (CardServiceException e){
            e.printStackTrace();
        }
        finally {

        }
    }

    private void startCountDown(final String text) {

        textView.setText(text + " in " + Integer.toString(COUNTDOWNSECONDS) + " seconds");
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
                //detectionProgressDialog.setMessage("Processing data");
                //detectionProgressDialog.show();

                //TEST
                releaseCamera();

               List<VideoFrame> frames = videoAnalyzer.filterFrames();
                System.out.println(frames.size());
                /*try {
                    videoAnalyzer.processData(challenge, new AsyncResponse<ChallengeResult>(){
                        @Override
                        public void processFinish(ChallengeResult result) {
                            if (result.getAccepted()){
                                textView.setText("ACCEPTED");
                            }else{
                                textView.setText("NOT ACCEPTED");
                            }
                        }
                    });
                } catch (GoogleMobileVisionMissingContextException e) {
                    e.printStackTrace();
                }*/

                //testShowPicturesInOrder(picturesFromVideo);
                //detectionProgressDialog.dismiss();
            }


        }).start();
        System.out.println("Challenge");

    }

    private void startCamera(){
        if (checkCameraHardware(getApplicationContext())){
            camera = getCameraInstance();
            if (camera != null){
                cameraPreview = new CameraPreview(this, camera);
                camera.setPreviewCallback(previewCallback);
                FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
                preview.addView(cameraPreview);
            }else{
                System.out.println("COULD NOT GET CAMERA");
            }

        }else{
            System.out.println("WE DONT HAVE CAMERA");
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
    public static Camera getCameraInstance(){
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
            System.out.println("FRONT CAMERA NOT AVAILABLE");
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
