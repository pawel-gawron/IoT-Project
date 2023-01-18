package com.example.sensehatclienfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LEDSending extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_sending);
    }

    public void goToDynamicList(View v){

        try {
            Intent intent = new Intent(this, DynamicList.class);
            startActivity(intent);

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

    public void goToChart(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        }
}
