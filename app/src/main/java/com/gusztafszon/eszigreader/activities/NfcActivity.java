package com.gusztafszon.eszigreader.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gusztafszon.eszigreader.R;
import com.gusztafszon.eszigreader.constants.Constants;
import com.gusztafszon.eszigreader.dialog.AddMRTDDialogFragment;
import com.gusztafszon.eszigreader.interfaces.IFragmentResult;
import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.LDSFileUtil;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.security.Security;


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

        imageView = (ImageView) findViewById(R.id.profilepicture);

        //updateUI();

        //checking whether nfc started this activity
        if (getIntent() == null || getIntent().getExtras() == null) {
            return;
        }
        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

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
            InputStream pictureInputStream = null;
            InputStream dg1InputStream = null;
            try{
                pictureInputStream = ps.getInputStream(PassportService.EF_DG2);
                DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, pictureInputStream);
                byte[] encodedPicture = dg2.getFaceInfos().get(0).getEncoded();

                System.out.println(dg2.getFaceInfos().get(0).getFaceImageInfos().get(0).getGender());

                Bitmap bmp = BitmapFactory.decodeStream(dg2.getFaceInfos().get(0).getFaceImageInfos().get(0).getImageInputStream());
                imageView.setImageBitmap(bmp);

                dg1InputStream = ps.getInputStream(PassportService.EF_DG1);
                DG1File dg1 = (DG1File)LDSFileUtil.getLDSFile(PassportService.EF_DG1, dg1InputStream);

                System.out.println("Doc code : " + dg1.getMRZInfo().getDocumentNumber());
                System.out.println("PATH : " + model.getIdServerPath());

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    pictureInputStream.close();
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
    }
}
