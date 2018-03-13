package com.example.android.bluetoothlegatt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.models.UrlDialogResponse;
import com.example.android.bluetoothlegatt.models.WifiDialogResponse;

public class WifiPrompter {

    Context context;
    private String mSsidResult;
    private String mPassResult;
    private DialogInterface.OnClickListener mCancelClickListener;

    private String networkType;

    public WifiPrompter(Activity activity) {

        this.context = activity;

        mCancelClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        };

    }

    public void setCancelClickListener(DialogInterface.OnClickListener listener) {
        this.mCancelClickListener = listener;
    }

    public String getResultSsid() {
        return mSsidResult;
    }
    public String getResultPass() {
        return mPassResult;
    }


    public void showDialog(final WifiDialogResponse response) {

        // get url_dialog.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.wifi_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set url_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText ssidInput = (EditText) promptsView
                .findViewById(R.id.wifi_dialog_ssid);
        final EditText passInput = (EditText) promptsView
                .findViewById(R.id.wifi_dialog_pass);

        //Initialise spinner components
        final Spinner networkSpinner = (Spinner) promptsView.findViewById(R.id.wifi_dialog_network_spinner);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(context, R.array.wifi_dialog_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSpinner.setAdapter(adapter);
        networkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                networkType = parent.getItemAtPosition(position).toString();
                if(networkType.equals("Open")) {
                    if(passInput.isEnabled()) {
                        passInput.setEnabled(false);
                        passInput.setText("");
                    }
                } else {
                    if(!passInput.isEnabled()) {
                        passInput.setEnabled(true);
                    }
                }
                Toast.makeText(context, "Wifi type chosen " + networkType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                mSsidResult = ssidInput.getText().toString();
                                mPassResult = passInput.getText().toString();
                                response.getDialogResponse(ssidInput, passInput, networkType);
                            }
                        })
                .setNegativeButton("Cancel",
                        mCancelClickListener);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
