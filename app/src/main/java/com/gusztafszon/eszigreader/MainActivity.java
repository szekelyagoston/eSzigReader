package com.gusztafszon.eszigreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gusztafszon.eszigreader.dialog.AddMRTDDialogFragment;
import com.gusztafszon.eszigreader.interfaces.IFragmentResult;
import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;
import com.gusztafszon.eszigreader.mrtd.registration.model.IdDocument;
import com.gusztafszon.eszigreader.mrtd.registration.model.MainActivityModel;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String MY_REFERENCES = "myRefs";
    private static final String DOCUMENT_NUMBER = "document_number";
    private static final String EXPIRATION_DATE = "expiration_date";
    private static final String DATE_OF_BIRTH = "date_of_birth";

    private MainActivityModel model = new MainActivityModel();

    SharedPreferences preferences;

    TextView documentNumberTextView;
    TextView expirationDateTextView;
    TextView dateOfBirthTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getting shared preferences
        preferences = getSharedPreferences(MY_REFERENCES, 0);
        setCurrentDocumentFromPreferences();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                //setDatePickers(dialog);
            }
        });

        FloatingActionButton startReadingButton = (FloatingActionButton) findViewById(R.id.start_reading);
        startReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("startreading");
            }
        });
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
        documentNumberTextView = (TextView) findViewById(R.id.documentnumber);
        expirationDateTextView = (TextView) findViewById(R.id.expirationdate);
        dateOfBirthTextView = (TextView) findViewById(R.id.dateofbirth);

        documentNumberTextView.setText(model.getDocument().getDocumentNumber());
        expirationDateTextView.setText(model.getDocument().getExpirationDate());
        dateOfBirthTextView.setText(model.getDocument().getDateOfBirth());


    }

}
