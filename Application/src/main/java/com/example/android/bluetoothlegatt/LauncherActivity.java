package com.example.android.bluetoothlegatt;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothlegatt.activities.DeviceScanActivity;

public class LauncherActivity extends Activity {

    private Button mViewDevicesBtn;
    private Button mBeginDcsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mViewDevicesBtn = findViewById(R.id.btn_view_devices);
        mBeginDcsBtn = findViewById(R.id.btn_start_dcs);

        mViewDevicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(LauncherActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });

    }

}
