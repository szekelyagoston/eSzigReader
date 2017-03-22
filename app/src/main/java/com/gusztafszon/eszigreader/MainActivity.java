package com.gusztafszon.eszigreader;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
    private TextView dateOfBirthTextView;

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

        String appUrl = getIntent().getStringExtra("appurl");
        String url = getIntent().getStringExtra("url");
        String uid = getIntent().getStringExtra("uid");
        String type = getIntent().getStringExtra("type");
        String username = getIntent().getStringExtra("username");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.APP_URL, appUrl);
        editor.putString(Constants.UID, uid);
        editor.putString(Constants.TYPE, type);
        editor.putString(Constants.URL, url);
        editor.putString(Constants.USERNAME, username);
        editor.commit();

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
