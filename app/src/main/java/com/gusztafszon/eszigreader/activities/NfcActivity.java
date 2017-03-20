package com.gusztafszon.eszigreader.activities;

import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;

import com.gusztafszon.eszigreader.R;
import com.gusztafszon.eszigreader.constants.Constants;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;
import com.gusztafszon.eszigreader.service.RestApi;

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


/**
 * Created by agoston.szekely on 2017.01.05..
 */

public class NfcActivity  extends AppCompatActivity {

    private MainActivityModel model = new MainActivityModel();

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.getProviders();
    }

    private SharedPreferences preferences;
    private ImageView imageView;

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
                System.out.println("PATH : " + model.getIdServerPath());
                System.out.println("UID : " + model.getUid());



                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                Callable<String> callable =  new RestApi(docId, model.getIdServerPath(), model.getUid());
                Future<String> future = executor.schedule(callable, 0, TimeUnit.MILLISECONDS);
                String result= future.get();

                System.out.println(result);

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

    private void setCurrentDocumentFromPreferences() {
        model.setDocument(new IdDocument(preferences.getString(Constants.DOCUMENT_NUMBER, ""), preferences.getString(Constants.EXPIRATION_DATE, ""), preferences.getString(Constants.DATE_OF_BIRTH, "")));
        model.setIdServerPath(preferences.getString(Constants.APP_URL, ""));
        model.setUid(preferences.getString(Constants.UID, ""));
    }
}
