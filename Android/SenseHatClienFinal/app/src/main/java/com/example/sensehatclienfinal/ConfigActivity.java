package com.example.sensehatclienfinal;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class ConfigActivity extends AppCompatActivity {
    /* BEGIN config TextViews */
    EditText ipEditText;
    EditText sampleTimeEditText;
    /* END config TextViews */
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // get the Intent that started this Activity
        Intent intent = getIntent();

        // get the Bundle that stores the data of this Activity
        Bundle configBundle = intent.getExtras();

        if(configBundle != null) {
            ipEditText = findViewById(R.id.ipEditTextConfig);
            String ip = configBundle.getString(Common.CONFIG_IP_ADDRESS, Common.DEFAULT_IP_ADDRESS);
            ipEditText.setText(ip);

            sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
            int st = configBundle.getInt(Common.CONFIG_SAMPLE_TIME, Common.DEFAULT_SAMPLE_TIME);
            sampleTimeEditText.setText(Integer.toString(st));
        }
    }

    @Override
    public void onBackPressed() {
        intent = new Intent();
        intent.putExtra(Common.CONFIG_IP_ADDRESS, ipEditText.getText().toString());
        intent.putExtra(Common.CONFIG_SAMPLE_TIME, sampleTimeEditText.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void goBack(View v){
        try {
            intent = new Intent();
            intent.putExtra(Common.CONFIG_IP_ADDRESS, ipEditText.getText().toString());
            intent.putExtra(Common.CONFIG_SAMPLE_TIME, sampleTimeEditText.getText().toString());
            setResult(RESULT_OK, intent);
            finish();

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

}
