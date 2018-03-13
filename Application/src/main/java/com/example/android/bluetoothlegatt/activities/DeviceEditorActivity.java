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
import com.example.android.bluetoothlegatt.models.DialogResponse;
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

    private UrlPrompter mUrlPrompt;
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

        mDcsParser = new DcsParser();

        mPopInstructionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String removedInstruction = mDcsParser.popInstruction();
                if(removedInstruction.equals("curl")) {
                    mDcsParser.popArgument();
                }
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
                mUrlPrompt.setCancelClickListener(mUrlPromptListener);
                mUrlPrompt.showDialog(new DialogResponse() {
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
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void updatePreview() {
        mInstructionPreview.setText("Current instructions: " + mDcsParser.instructionsToString());
        mArgumentPreview.setText("Current arguments: " + mDcsParser.argumentsToString());
    }

    private DialogInterface.OnClickListener mUrlPromptListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, "INFO: Cancelling URL dialog");
            dialog.cancel();
        }
    };

}
