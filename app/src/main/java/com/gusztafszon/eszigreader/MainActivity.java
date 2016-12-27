package com.gusztafszon.eszigreader;

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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.gusztafszon.eszigreader.dialog.AddMRTDDialogFragment;
import com.gusztafszon.eszigreader.interfaces.IFragmentResult;
import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.lds.FaceImageInfo;
import org.jmrtd.lds.FaceInfo;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.LDSFileUtil;

import java.io.InputStream;
import java.security.Security;


public class MainActivity extends AppCompatActivity {

    private static final String MY_REFERENCES = "myRefs";
    private static final String DOCUMENT_NUMBER = "document_number";
    private static final String EXPIRATION_DATE = "expiration_date";
    private static final String DATE_OF_BIRTH = "date_of_birth";

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.getProviders();
        Log.i("Tag", String.format("Cipher algo: %s", Security.getAlgorithms("Cipher").toString()));
        Log.i("Tag", String.format("Mac algo: %s", Security.getAlgorithms("Mac").toString()));
    }

    private MainActivityModel model = new MainActivityModel();

    SharedPreferences preferences;

    private TextView documentNumberTextView;
    private TextView expirationDateTextView;
    private TextView dateOfBirthTextView;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getting shared preferences
        preferences = getSharedPreferences(MY_REFERENCES, 0);
        setCurrentDocumentFromPreferences();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        documentNumberTextView = (TextView) findViewById(R.id.documentnumber);
        expirationDateTextView = (TextView) findViewById(R.id.expirationdate);
        dateOfBirthTextView = (TextView) findViewById(R.id.dateofbirth);
        imageView = (ImageView) findViewById(R.id.profilepicture);

        updateUI();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddMRTDDialogFragment dialog = new AddMRTDDialogFragment();
                dialog.setResultCallback(new IFragmentResult<MRTDRegistrationDto>() {
                    @Override
                    public void onResult(MRTDRegistrationDto result) {
                        model.setDocument(result);
                        saveResultToPreferences(result);
                        updateUI();
                    }
                });
                FragmentManager ft = getSupportFragmentManager();
                dialog.show(ft, "mrtsDialogFragment");

            }
        });

        FloatingActionButton startReadingButton = (FloatingActionButton) findViewById(R.id.start_reading);
        startReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReading();
            }
        });

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

            try{
                pictureInputStream = ps.getInputStream(PassportService.EF_DG2);
                DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, pictureInputStream);
                byte[] encodedPicture = dg2.getFaceInfos().get(0).getEncoded();

                System.out.println(dg2.getFaceInfos().get(0).getFaceImageInfos().get(0).getGender());

                Bitmap bmp = BitmapFactory.decodeStream(dg2.getFaceInfos().get(0).getFaceImageInfos().get(0).getImageInputStream());
                imageView.setImageBitmap(bmp);

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

    private void startReading() {
        //NFC MUST BE ENABLED
        
    }

    private void setCurrentDocumentFromPreferences() {
        model.setDocument(new IdDocument(preferences.getString(DOCUMENT_NUMBER, ""), preferences.getString(EXPIRATION_DATE, ""), preferences.getString(DATE_OF_BIRTH, "")));
    }

    private void saveResultToPreferences(MRTDRegistrationDto result) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DOCUMENT_NUMBER, result.getDocumentNumber());
        editor.putString(EXPIRATION_DATE, result.getExpirationDate());
        editor.putString(DATE_OF_BIRTH, result.getDateOfBirth());
        editor.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {


        documentNumberTextView.setText(model.getDocument().getDocumentNumber());
        expirationDateTextView.setText(model.getDocument().getFormattedExpirationDate());
        dateOfBirthTextView.setText(model.getDocument().getFormattedDateOfBirth());

    }

}
