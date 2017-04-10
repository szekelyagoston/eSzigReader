package com.gusztafszon.eszigreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gusztafszon.eszigreader.constants.Constants;
import com.gusztafszon.eszigreader.dialog.AddMRTDDialogFragment;
import com.gusztafszon.eszigreader.interfaces.IFragmentResult;
import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;

import java.util.List;


public class MainActivity extends AppCompatActivity {


    private MainActivityModel model = new MainActivityModel();

    private SharedPreferences preferences;

    private TextView documentNumberTextView;
    private TextView expirationDateTextView;
    private TextView mainDisplayTextView;

    private TextView dateOfBirthTextView;

    private static final String ERROR_COLOR="#FF0000";
    private static final String WARNING_COLOR="#CCCC00";
    private static final String SUCCESS_COLOR="#009900";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getting shared preferences
        preferences = getSharedPreferences(Constants.MY_REFERENCES, 0);
        setCurrentDocumentFromPreferences();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        documentNumberTextView = (TextView) findViewById(R.id.documentnumber);
        expirationDateTextView = (TextView) findViewById(R.id.expirationdate);
        dateOfBirthTextView = (TextView) findViewById(R.id.dateofbirth);

        mainDisplayTextView = (TextView) findViewById(R.id.mainDisplayTextView);

        updateUI();

        model.setIdServerPath(getIntent().getStringExtra("appurl"));
        model.setUrl(getIntent().getStringExtra("url"));
        model.setUid(getIntent().getStringExtra("uid"));
        model.setType(getIntent().getStringExtra("type"));
        model.setUserName(getIntent().getStringExtra("username"));

        checkNfcEnabled();

        setMainDisplay();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.APP_URL, model.getIdServerPath());
        editor.putString(Constants.UID, model.getUid());
        editor.putString(Constants.TYPE, model.getType());
        editor.putString(Constants.URL, model.getUrl());
        editor.putString(Constants.USERNAME, model.getUserName());
        editor.commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddMRTDDialogFragment dialog = new AddMRTDDialogFragment();

                Bundle bundle = new Bundle(3);
                bundle.putString(Constants.DOCUMENT_NUMBER, model.getDocument().getDocumentNumber());
                bundle.putString(Constants.EXPIRATION_DATE, model.getDocument().getExpirationDate());
                bundle.putString(Constants.DATE_OF_BIRTH, model.getDocument().getDateOfBirth());

                dialog.setArguments(bundle);

                dialog.setResultCallback(new IFragmentResult<MRTDRegistrationDto>() {
                    @Override
                    public void onResult(MRTDRegistrationDto result) {
                        model.setDocument(result);
                        saveResultToPreferences(result);
                        setMainDisplay();
                        updateUI();
                    }
                });
                FragmentManager ft = getSupportFragmentManager();
                dialog.show(ft, "mrtsDialogFragment");

            }
        });

    }

    private void checkNfcEnabled() {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.nfc);
        NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {

            mainDisplayTextView.setText("NFC not enabled or unavailable! Please turn it on with the bottom left button!");
            mainDisplayTextView.setTextColor(Color.parseColor(ERROR_COLOR));
            mainDisplayTextView.setTypeface(null, Typeface.BOLD);

            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(intent);
                    }
                }
            });

            model.setNfcEnabled(false);
        }else{
            model.setNfcEnabled(true);
            fab.setVisibility(View.GONE);
        }
    }

    private void setMainDisplay() {

        if (!model.isNfcEnabled()){
            return;
        }

        if (model.getType() == null){
            mainDisplayTextView.setText("App should not be runned directly! Please use this app only when logging in or registering to a page!");
            mainDisplayTextView.setTextColor(Color.parseColor(ERROR_COLOR));
            mainDisplayTextView.setTypeface(null, Typeface.BOLD);
        }else{
            switch (model.getType()){
                case "L" : {
                    if (model.getIdServerPath() == null || model.getUid() == null){
                        mainDisplayTextView.setText("There was an error retrieving parameters, login can not be continued! \nPlease try later!");
                        mainDisplayTextView.setTextColor(Color.parseColor(ERROR_COLOR));
                        mainDisplayTextView.setTypeface(null, Typeface.BOLD);
                    }else{
                        if (!model.getDocument().isValid()){
                            mainDisplayTextView.setText("Please add your card information by clicking on the bottom button!");
                            mainDisplayTextView.setTextColor(Color.parseColor(WARNING_COLOR));
                        }else{
                            mainDisplayTextView.setText("Please hold your card to the back of your phone!");
                            mainDisplayTextView.setTextColor(Color.parseColor(SUCCESS_COLOR));
                        }
                        mainDisplayTextView.setTypeface(null, Typeface.BOLD);
                    }
                    break;
                }
                case "R" : {
                    if (model.getUrl() == null || model.getUserName() == null){
                        mainDisplayTextView.setText("There was an error retrieving parameters, registration can not be continued! \nPlease try later!");
                        mainDisplayTextView.setTextColor(Color.parseColor(ERROR_COLOR));
                        mainDisplayTextView.setTypeface(null, Typeface.BOLD);
                    }else{
                        if (!model.getDocument().isValid()){
                            mainDisplayTextView.setText("Please add your card information by clicking on the bottom button!");
                            mainDisplayTextView.setTextColor(Color.parseColor(WARNING_COLOR));
                        }else{
                            mainDisplayTextView.setText("Please hold your card to the back of your phone!");
                            mainDisplayTextView.setTextColor(Color.parseColor(SUCCESS_COLOR));
                        }
                        mainDisplayTextView.setTypeface(null, Typeface.BOLD);
                    }
                    break;
                }
                default : {
                    mainDisplayTextView.setText("There was an error retrieving parameters! \nPlease try later!");
                    mainDisplayTextView.setTextColor(Color.parseColor(ERROR_COLOR));
                    mainDisplayTextView.setTypeface(null, Typeface.BOLD);
                }
            }
        }
    }


    private void setCurrentDocumentFromPreferences() {
        model.setDocument(new IdDocument(preferences.getString(Constants.DOCUMENT_NUMBER, ""), preferences.getString(Constants.EXPIRATION_DATE, ""), preferences.getString(Constants.DATE_OF_BIRTH, "")));
    }

    private void saveResultToPreferences(MRTDRegistrationDto result) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.DOCUMENT_NUMBER, result.getDocumentNumber());
        editor.putString(Constants.EXPIRATION_DATE, result.getExpirationDate());
        editor.putString(Constants.DATE_OF_BIRTH, result.getDateOfBirth());
        editor.commit();

    }

    private void updateUI() {
        documentNumberTextView.setText(model.getDocument().getDocumentNumber());
        expirationDateTextView.setText(model.getDocument().getFormattedExpirationDate());
        dateOfBirthTextView.setText(model.getDocument().getFormattedDateOfBirth());

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkNfcEnabled();
        setMainDisplay();
    }
}
