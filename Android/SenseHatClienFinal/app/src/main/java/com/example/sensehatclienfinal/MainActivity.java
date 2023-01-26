package com.example.sensehatclienfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.EventLogTags;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private Button buttonDynamicList;
    String url;
    private TextView urlAdress;
    private TextView textViewSampleTime;
    RequestQueue queue;
    JSONObject responseTemperature;
    JSONObject responseHumidity;
    JSONObject responsePressure;
    JSONObject responsePitch;
    JSONObject responseRoll;
    JSONObject responseYaw;
    TextView text;
    Timer timer;
    int k = 0; //!< Samples counter
    float temperature;
    float humidity;
    float pressure;
    float pitch;
    float roll;
    float yaw;
    Boolean extendChart = false;
    float originalSignal;
    public ArrayList<Float> angleVector = new ArrayList<>();
    private GraphView chart; //!< GraphView object
    private LineGraphSeries[] signal;
    int sampleMax;
    private RadioButton optionTemp;
    private RadioButton optionPress;
    private RadioButton optionHum;
    private RadioButton optionRoll;
    private RadioButton optionPitch;
    private RadioButton optionYaw;
    private String optionChecked = null;
    double minY = -35.0;
    double maxY = 110.0;
    String titleOriginal = "Temperature";
    String titleFiltered = "Filtered Temperature";
    String yAxisTitle = "degC";
    /* BEGIN config data */
    private String ipAddress = Common.DEFAULT_IP_ADDRESS;
    private int sampleTime = Common.DEFAULT_SAMPLE_TIME;
    /* END config data */

    
    private IIRFIlter filter = new IIRFIlter(IIRFilterData.feedforward_coefficients, IIRFilterData.feedbackward_coefficients,
            IIRFilterData.stateforward, IIRFilterData.statebackward);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlAdress= (TextView) findViewById(R.id.urlAdress);
        urlAdress.setText(getIpAddressDisplayText(ipAddress));

        textViewSampleTime = findViewById(R.id.textViewSampleTime);
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));
        ChartInit();

        sampleMax = 100*(100/sampleTime);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        if ((requestCode == Common.REQUEST_CODE_CONFIG) && (resultCode == RESULT_OK)) {

            // IoT server IP address
            ipAddress = dataIntent.getStringExtra(Common.CONFIG_IP_ADDRESS);
            urlAdress.setText(getIpAddressDisplayText(ipAddress));

            //Sample time (ms)
            String sampleTimeText = dataIntent.getStringExtra(Common.CONFIG_SAMPLE_TIME);
            sampleTime = Integer.parseInt(sampleTimeText);
            textViewSampleTime.setText(getSampleTimeDisplayText(sampleTimeText));

            sampleMax = 100*(100/sampleTime);
        }
    }

    private void openConfig() {
        Intent openConfigIntent = new Intent(this, ConfigActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putString(Common.CONFIG_IP_ADDRESS, ipAddress);
        configBundle.putInt(Common.CONFIG_SAMPLE_TIME, sampleTime);
        openConfigIntent.putExtras(configBundle);
        startActivityForResult(openConfigIntent, Common.REQUEST_CODE_CONFIG);
    }

    private String getIpAddressDisplayText(String ip) {
        return ("IP: " + ip);
    }

    private String getSampleTimeDisplayText(String st) {
        return ("Sample time: " + st + " ms");
    }

    public void goToDynamicList(View v){

        try {
            Intent intent = new Intent(this, DynamicList.class);
            startActivity(intent);

            if(timer != null) {
                timer.cancel();
                timer = null;
            }

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

    public void goToLED(View v){
        try {
        Intent intent = new Intent(this, LEDSending.class);
        startActivity(intent);

        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

    public void goToConfigActivity(View v){

        try {
            openConfig();

            if(timer != null) {
                timer.cancel();
                timer = null;
            }

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

    public void RunButton(View v) throws ExecutionException, InterruptedException {

        chart.removeSeries(signal[0]);
        chart.removeSeries(signal[1]);
        
        switch (radioButtonCheck())
        {
            case "Temp":
                minY = -35.0;
                maxY = 110.0;
                titleOriginal = "Temperature";
                titleFiltered = "Filtered Temperature";
                yAxisTitle = "degC";
                ChartInit();

                break;
            case "Press":
                minY = 200.0;
                maxY = 1300.0;
                titleOriginal = "Pressure";
                titleFiltered = "Filtered Pressure";
                yAxisTitle = "hPa";
                ChartInit();

                break;
            case "Hum":
                minY = 0.0;
                maxY = 360.0;
                titleOriginal = "Humidity";
                titleFiltered = "Filtered Humidity";
                yAxisTitle = "%";
                ChartInit();

                break;
            case "Pitch":
                minY = 0.0;
                maxY = 360.0;
                titleOriginal = "Angle Pitch";
                titleFiltered = "Filtered Pitch";
                yAxisTitle = "deg";
                ChartInit();

                break;
            case "Roll":
                minY = 0.0;
                maxY = 360.0;
                titleOriginal = "Angle Yaw";
                titleFiltered = "Filtered Yaw";
                yAxisTitle = "deg";
                ChartInit();

                break;
            case "Yaw":
                minY = -180.0;
                maxY = 180.0;
                titleOriginal = "Angle Roll";
                titleFiltered = "Filtered Roll";
                yAxisTitle = "deg";
                ChartInit();

                break;
            default:
                break;
        }

        if(timer == null) {
            k = 0;

            signal[0].resetData(new DataPoint[]{});
            signal[1].resetData(new DataPoint[]{});

            timer = new Timer();
            TimerTask filterTimerTask = new TimerTask() {
                public void run() {
                    try {
                        server();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ; }
            };
            timer.scheduleAtFixedRate(filterTimerTask, 0, (int)(sampleTime));
        }
    }

    public void StopButton(View v){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void server() throws ExecutionException, InterruptedException, TimeoutException, JSONException {

        if (k <= sampleMax) {
            if (queue == null) {
                queue = Volley.newRequestQueue(this.getApplicationContext());
            }

            url = "http://" + ipAddress + "/get_measurements";

//         Create future request
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, future, future);

            // Add the request to the RequestQueue.
            queue.add(jsonRequest);

            responseTemperature = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get("temperature");
            responseHumidity = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get("humidity");
            responsePressure = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get("pressure");
            responsePitch = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get("pitch");
            responseRoll = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get("roll");
            responseYaw = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get("yaw");

            temperature = Float.parseFloat(responseTemperature.getString("value"));
            humidity = Float.parseFloat(responseHumidity.getString("value"));
            pressure = Float.parseFloat(responsePressure.getString("value"));
            roll = Float.parseFloat(responseRoll.getString("value"));
            pitch = Float.parseFloat(responsePitch.getString("value"));
            yaw = Float.parseFloat(responseYaw.getString("value"));

            switch (radioButtonCheck())
            {
                case "Temp":
                    originalSignal = temperature;

                    break;
                case "Press":
                    originalSignal = pressure;

                    break;
                case "Hum":
                    originalSignal = humidity;

                    break;
                case "Roll":
                    originalSignal = roll;

                    break;
                case "Pitch":
                    originalSignal = pitch;

                    break;
                case "Yaw":
                    originalSignal = yaw;

                    break;
                default:
                    break;
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final Double xf = filter.Execute(Double.valueOf(originalSignal));

                    signal[0].appendData(new DataPoint(k * (sampleTime / 1000.0), originalSignal), false, sampleMax);
                    signal[1].appendData(new DataPoint(k * (sampleTime / 1000.0), xf), false, sampleMax);
                    chart.onDataChanged(true, true);
                }
            });
            k++;
        }else{
            timer.cancel();
            timer = null;}
    }

    String radioButtonCheck(){
        optionTemp = findViewById(R.id.op1);
        optionPress = findViewById(R.id.op2);
        optionHum = findViewById(R.id.op3);
        optionRoll = findViewById(R.id.op4);
        optionPitch = findViewById(R.id.op5);
        optionYaw = findViewById(R.id.op6);

        if (optionTemp.isChecked()) optionChecked = "Temp";
        else if (optionPress.isChecked()) optionChecked = "Press";
        else if (optionHum.isChecked()) optionChecked = "Hum";
        else if (optionRoll.isChecked()) optionChecked = "Roll";
        else if (optionPitch.isChecked()) optionChecked = "Pitch";
        else if (optionYaw.isChecked()) optionChecked = "Yaw";
        return optionChecked;
    }

    private void ChartInit() {
        // https://github.com/jjoe64/GraphView/wiki
        chart = (GraphView)findViewById(R.id.chart);

        signal = new LineGraphSeries[]{new LineGraphSeries<>(new DataPoint[]{}),
                new LineGraphSeries<>(new DataPoint[]{})};
        chart.addSeries(signal[0]);
        chart.addSeries(signal[1]);

        chart.getViewport().setXAxisBoundsManual(true);
        chart.getViewport().setMinX(0.0);
        chart.getViewport().setMaxX(10.0);
        chart.getViewport().setYAxisBoundsManual(true);
        chart.getViewport().setMinY(minY);
        chart.getViewport().setMaxY(maxY);

        signal[0].setTitle(titleOriginal);
        signal[0].setColor(Color.BLUE);
        signal[1].setTitle(titleFiltered);
        signal[1].setColor(Color.RED);

        chart.getLegendRenderer().setVisible(true);
        chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        chart.getLegendRenderer().setTextSize(30);

        chart.getGridLabelRenderer().setTextSize(20);
        chart.getGridLabelRenderer().setVerticalAxisTitle(Space(7) + yAxisTitle);
        chart.getGridLabelRenderer().setHorizontalAxisTitle(Space(11) + "Time [s]");
        chart.getGridLabelRenderer().setNumHorizontalLabels(9);
        chart.getGridLabelRenderer().setNumVerticalLabels(7);
        chart.getGridLabelRenderer().setPadding(35);
    }

    private String Space(int n) {
        return new String(new char[n]).replace('\0', ' ');
    }
}