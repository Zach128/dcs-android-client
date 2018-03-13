package com.example.android.bluetoothlegatt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.models.DialogResponse;

public class UrlPrompter {

    Context context;
    private Button button;
    private String mResult;
    private DialogInterface.OnClickListener mCancelClickListener;

    public UrlPrompter(Activity activity) {

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

    public String getResultText() {
        return mResult;
    }

    public void showDialog(final DialogResponse response) {

        // get url_dialog.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.url_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set url_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.url_dialog_field);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                mResult = userInput.getText().toString();
                                response.getUrlResponse(userInput);
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
