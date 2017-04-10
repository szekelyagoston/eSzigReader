package com.gusztafszon.eszigreader.dialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.gusztafszon.eszigreader.R;
import com.gusztafszon.eszigreader.constants.Constants;
import com.gusztafszon.eszigreader.interfaces.IFragmentResult;
import com.gusztafszon.eszigreader.mrtd.registration.dto.MRTDRegistrationDto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gusztafszon on 2016.11.30..
 */

public class AddMRTDDialogFragment extends DialogFragment {

    private static final String DATE_FORMAT = "dd/MM/yy";

    private Context context;
    private Date expDate;
    private Date dateOfBirth;

    private IFragmentResult<MRTDRegistrationDto> callback;

    //from DOCS: https://developer.android.com/guide/topics/ui/dialogs.html
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final String documentNumber = getArguments().getString(Constants.DOCUMENT_NUMBER);
        String expirationDate = getArguments().getString(Constants.EXPIRATION_DATE);
        String birthDate = getArguments().getString(Constants.DATE_OF_BIRTH);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_add_mrtd, null))
                // Add action buttons
                .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = getDialog();
                        EditText docNumber = (EditText)d.findViewById(R.id.document_number);
                        //docNumber.setText(documentNumber);
                        callback.onResult(new MRTDRegistrationDto(docNumber.getText().toString(), expDate, dateOfBirth));
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddMRTDDialogFragment.this.getDialog().cancel();
                    }
                });


        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = this.getDialog();

        EditText dateOfBirthEditText = (EditText) dialog.findViewById(R.id.date_of_birth);
        EditText expDateEditText = (EditText) dialog.findViewById(R.id.exp_date);

        addDatePickerToEditText(dateOfBirthEditText);
        addDatePickerToEditText(expDateEditText);


    }

    private void addDatePickerToEditText(final EditText field) {
        //http://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext

        final Calendar cal = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener datePickerDialogListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

                field.setText(sdf.format(cal.getTime()));
                if (R.id.date_of_birth == field.getId()){
                    dateOfBirth = cal.getTime();
                }
                if (R.id.exp_date == field.getId()){
                    expDate = cal.getTime();
                };

            }

        };

        field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, android.R.style.Widget_Material_Light_DatePicker, datePickerDialogListener, cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.context = context;
    }

    public void setResultCallback(IFragmentResult<MRTDRegistrationDto> cb){
        this.callback = cb;
    }
}
