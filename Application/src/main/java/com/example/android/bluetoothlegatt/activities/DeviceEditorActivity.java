package com.example.android.bluetoothlegatt.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.WifiController;
import com.example.android.bluetoothlegatt.models.UrlDialogResponse;
import com.example.android.bluetoothlegatt.models.WifiDialogResponse;
import com.example.android.bluetoothlegatt.utils.DcsParser;

public class DeviceEditorActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final String TAG = DeviceEditorActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Spinner mInstructionSpinner;
    private TextView mInstructionPreview;
    private TextView mArgumentPreview;
    private Button mPopInstructionBtn;
    private Button mCommitCharsBtn;
    private Button mPopAndProcessBtn;

    private UrlPrompter mUrlPrompt;
    private WifiPrompter mWifiPrompt;
    private DcsParser mDcsParser;

    private boolean mSpinnerInitialised = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_editor);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialise spinner components
        mInstructionSpinner = (Spinner) findViewById(R.id.instruction_spinner);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.dcs_ins_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInstructionSpinner.setAdapter(adapter);
        mInstructionSpinner.setOnItemSelectedListener(this);

        mInstructionPreview = (TextView) findViewById(R.id.txt_cic_preview);
        mArgumentPreview = (TextView) findViewById(R.id.txt_iac_preview);
        mPopInstructionBtn = (Button) findViewById(R.id.btn_pop_instruction);
        mCommitCharsBtn = (Button) findViewById(R.id.btn_commit_chars);
        mPopAndProcessBtn = (Button) findViewById(R.id.btn_pop_and_process);

        mDcsParser = new DcsParser(DeviceEditorActivity.this)
                .setWifiController(new WifiController(DeviceEditorActivity.this));

        mPopInstructionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String removedInstruction = mDcsParser.popInstruction();
                if(removedInstruction.equals("curl")) {
                    mDcsParser.popArgument();
                } else if(removedInstruction.equals("conwf")) {
                    mDcsParser.popArgument();
                    mDcsParser.popArgument();
                    mDcsParser.popArgument();
                }
                updatePreview();
            }
        });

        mPopAndProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDcsParser.processTopInstruction();
                updatePreview();
            }
        });

        /* TODO: Insert characteristic writing code */

        updatePreview();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(!mSpinnerInitialised) {
            mSpinnerInitialised = true;
            return;
        }

        String item = parent.getItemAtPosition(position).toString();

        int colonIndex = item.indexOf(':');

        item = item.substring(0, colonIndex);

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Adding: " + item, Toast.LENGTH_LONG).show();

        switch(item) {
            case "ms":
            case "ums":
            case "actwf":
            case "sdwf":
                mDcsParser.pushInstruction(item);
                updatePreview();
                break;
            case "curl":
                mUrlPrompt = new UrlPrompter(this);
                mUrlPrompt.setCancelClickListener(mPromptCancelListener);
                mUrlPrompt.showDialog(new UrlDialogResponse() {
                    @Override
                    public void getUrlResponse(EditText textField) {
                        String result = textField.getText().toString();
                        if(TextUtils.isEmpty(result)) {
                            return;
                        }

                        mDcsParser.pushInstruction("curl");
                        mDcsParser.pushArgument(result);
                        updatePreview();
                        Toast.makeText(DeviceEditorActivity.this, "Received URL " + result, Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case "conwf":
                mWifiPrompt = new WifiPrompter(this);
                mWifiPrompt.setCancelClickListener(mPromptCancelListener);
                mWifiPrompt.showDialog(new WifiDialogResponse() {
                    @Override
                    public void getDialogResponse(EditText ssidText, EditText passText, String networkType) {
                        String ssid = ssidText.getText().toString();
                        String pass = passText.getText().toString();

                        //Check that valid input is obtained
                        if(TextUtils.isEmpty(ssid) || (!networkType.equals("Open") && TextUtils.isEmpty(pass))) {
                            return;
                        }

                        mDcsParser.pushInstruction("conwf");
                        mDcsParser.pushArgument(ssid);

                        if(networkType.equals("Open")) {
                            mDcsParser.pushArgument("<n>");
                        } else {
                            mDcsParser.pushArgument(pass);
                        }

                        mDcsParser.pushArgument(networkType);

                        updatePreview();
                        Toast.makeText(DeviceEditorActivity.this, "WiFi network added " + ssid, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void updatePreview() {
        mInstructionPreview.setText("Current instructions: ".concat(mDcsParser.instructionsToString()));
        mArgumentPreview.setText("Current arguments: ".concat(mDcsParser.argumentsToString()));
    }

    private DialogInterface.OnClickListener mPromptCancelListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, "INFO: Cancelling URL dialog");
            dialog.cancel();
        }
    };

}
