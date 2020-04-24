package com.tmo.tr.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Preferences extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        String ip_address = getIntent().getStringExtra("ip_address");
        String resolution = getIntent().getStringExtra("resolution");
        ((TextView)findViewById(R.id.ip_address_val)).setText(ip_address.toCharArray(), 0, ip_address.length());
        RadioButton b = null;

        switch(resolution) {
            case "1080p":
                b = (RadioButton) findViewById(R.id.res1080p);
                break;
            case "720p":
                b = (RadioButton) findViewById(R.id.res720p);
                break;
//            case "800":
//                b = (RadioButton) findViewById(R.id.res800);
//                break;
            case "640":
                b = (RadioButton) findViewById(R.id.res640);
                break;
            case "320":
                b = (RadioButton) findViewById(R.id.res320);
                break;
            case "176":
                b = (RadioButton) findViewById(R.id.res176);
                break;
            default:
                b = (RadioButton) findViewById(R.id.res720p);
                break;
        }

        b.setChecked(true);

        Button close = (Button) findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                String ip_address = ((TextView)findViewById(R.id.ip_address_val)).getText().toString();
                int selectedId = ((RadioGroup)findViewById(R.id.resolution_group)).getCheckedRadioButtonId();
                String resolution = ((RadioButton) findViewById(selectedId)).getText().toString();
                intent.putExtra("ip_address", ip_address);
                intent.putExtra("resolution", resolution);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}