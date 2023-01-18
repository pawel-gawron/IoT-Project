package com.example.sensehatclienfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
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

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private Button buttonDynamicList;
    EditText urlAdress;
    Button buttonStart;
    Button buttonStop;
    RequestQueue queue;
    JSONObject responseTemperature;
    JSONObject responseHumidity;
    JSONObject responsePressure;
    TextView text;
    Timer timer;
    int k = 0; //!< Samples counter
    float temperature;
    float humidity;
    float pressure;
    private GraphView chart; //!< GraphView object
    private LineGraphSeries[] signal;
    int sampleMax;
    DataPoint[] dataTemperature;
    DataPoint[] dataPressure;
    
    private IIRFIlter filter = new IIRFIlter(IIRFilterData.feedforward_coefficients, IIRFilterData.feedbackward_coefficients,
            IIRFilterData.stateforward, IIRFilterData.statebackward);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStop = (Button) findViewById(R.id.stopButton);
        buttonStart = (Button) findViewById(R.id.startButton);
        urlAdress= (EditText) findViewById(R.id.urlAdress);
        buttonDynamicList = (Button) findViewById(R.id.buttonGoToDynamicList);

        ChartInit();

        dataPressure = new DataPoint[1000];
        dataTemperature = new DataPoint[1000];

//        sampleMax = (int)(chart.getViewport().getMaxX(false) / 100);
        sampleMax = 100;

//        button.setOnClickListener(
//                new View.OnClickListener()
//                {
//                    public void onClick(View view)
//                    {
//                        Log.v("EditText", urlAdress.getText().toString());
//                    }
//                });
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
        Intent intent = new Intent(this, LEDSending.class);
        startActivity(intent);

        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void RunButton(View v) throws ExecutionException, InterruptedException {
//        text = (TextView) findViewById(R.id.response);

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
            timer.scheduleAtFixedRate(filterTimerTask, 0, (int)(100));
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
            queue = Volley.newRequestQueue(this.getApplicationContext());

//         Create future request
            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, urlAdress.getText().toString(), null, future, future);
//        Log.v("Response", urlAdress.getText().toString());

            // Add the request to the RequestQueue.
            queue.add(jsonRequest);

            responseTemperature = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get(0);
            responseHumidity = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get(1);
            responsePressure = (JSONObject) future.get(100, TimeUnit.MILLISECONDS).get(2);

            temperature = Float.parseFloat(responseTemperature.getString("value"));
            humidity = Float.parseFloat(responseHumidity.getString("value"));
            pressure = Float.parseFloat(responsePressure.getString("value"));

//        text.setText(responseTemperature.toString());
//        text.setText(responseHumidity.toString());


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                if (k>=sampleMax-10){
////                    signal[0].resetData(dataTemperature);
////                    signal[1].resetData(dataPressure);
//                    signal[0].resetData(new DataPoint[]{});
//                    signal[1].resetData(new DataPoint[]{});
//                }
                    final Double xf = filter.Execute(Double.valueOf(temperature));
//                    Log.v("Filtered signal", xf.toString());
                    signal[0].appendData(new DataPoint(k * 0.1, temperature), false, sampleMax);
//                Log.v("Temperature", String.valueOf(temperature));
                    signal[1].appendData(new DataPoint(k * 0.1, xf), false, sampleMax);
                    chart.onDataChanged(true, true);
                    dataTemperature[k] = new DataPoint(k*0.1, temperature);
                    dataPressure[k] = new DataPoint(k*0.1, xf);
                }
            });
            k++;
        }else{
            timer.cancel();
            timer = null;}
    }

    private void ChartInit() {
        // https://github.com/jjoe64/GraphView/wiki
        chart = (GraphView)findViewById(R.id.chart);
        signal = new LineGraphSeries[]{ new LineGraphSeries<>(new DataPoint[]{}),
                new LineGraphSeries<>(new DataPoint[]{})};
        chart.addSeries(signal[0]);
        chart.addSeries(signal[1]);

//        chart.getSecondScale().addSeries(signal[1]);

//        chart.getSecondScale().setMinY(260);
//        chart.getSecondScale().setMaxY(1260);
//        chart.getSecondScale().setMinY(-30);
//        chart.getSecondScale().setMaxY(105);
//        chart.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.RED);

        chart.getViewport().setXAxisBoundsManual(true);
        chart.getViewport().setMinX(0.0);
        chart.getViewport().setMaxX(10.0);
        chart.getViewport().setYAxisBoundsManual(true);
        chart.getViewport().setMinY(-30.0);
        chart.getViewport().setMaxY(105.0);

        signal[0].setTitle("Temperature");
        signal[0].setColor(Color.BLUE);
        signal[1].setTitle("Pressure");
        signal[1].setColor(Color.RED);

        chart.getLegendRenderer().setVisible(true);
        chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        chart.getLegendRenderer().setTextSize(30);

        chart.getGridLabelRenderer().setTextSize(20);
        chart.getGridLabelRenderer().setVerticalAxisTitle(Space(7) + "Amplitude [-]");
        chart.getGridLabelRenderer().setHorizontalAxisTitle(Space(11) + "Time [s]");
        chart.getGridLabelRenderer().setNumHorizontalLabels(9);
        chart.getGridLabelRenderer().setNumVerticalLabels(7);
        chart.getGridLabelRenderer().setPadding(35);
    }

    private String Space(int n) {
        return new String(new char[n]).replace('\0', ' ');
    }
}