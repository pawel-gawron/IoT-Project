package com.example.sensehatclienfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DynamicList extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;

    private TextView urlAdressList;;
    private TextView sampleTimeList;
    String url;
    RequestQueue queue;
    JSONArray responseName;
    JSONArray responseValue;
    JSONArray responseUnit;
    TextView name;
    TextView value;
    TextView unit;
    Timer timerList;
    LinearLayoutManager manager;
    /* BEGIN config data */
    private String ipAddress = Common.DEFAULT_IP_ADDRESS;
    private int sampleTime = Common.DEFAULT_SAMPLE_TIME;
    /* END config data */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_list);

        urlAdressList= (TextView) findViewById(R.id.urlAdressList);
        urlAdressList.setText(getIpAddressDisplayText(ipAddress));

        sampleTimeList = findViewById(R.id.textViewSampleTime);
        sampleTimeList.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        if ((requestCode == Common.REQUEST_CODE_CONFIG) && (resultCode == RESULT_OK)) {

            // IoT server IP address
            ipAddress = dataIntent.getStringExtra(Common.CONFIG_IP_ADDRESS);
            urlAdressList.setText(getIpAddressDisplayText(ipAddress));

            //Sample time (ms)
            String sampleTimeText = dataIntent.getStringExtra(Common.CONFIG_SAMPLE_TIME);
            sampleTime = Integer.parseInt(sampleTimeText);
            sampleTimeList.setText(getSampleTimeDisplayText(sampleTimeText));
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

    public void goToChart(View v){
        try {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        if(timerList != null) {
            timerList.cancel();
            timerList = null;
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

        if(timerList != null) {
            timerList.cancel();
            timerList = null;
        }
        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

    public void goToConfigActivity(View v){

        try {
            openConfig();

            if(timerList != null) {
                timerList.cancel();
                timerList = null;
            }

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

    public void refresh(View v){

        if(timerList == null) {

            timerList = new Timer();
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
            timerList.scheduleAtFixedRate(filterTimerTask, 0, (int)(sampleTime));
        }
    }

    public void stop(View v) {
        if(timerList != null) {
            timerList.cancel();
            timerList = null;
        }
    }

    public void server() throws ExecutionException, InterruptedException, TimeoutException, JSONException {
        if (queue == null) {
            queue = Volley.newRequestQueue(this.getApplicationContext());
        }

        url = "http://" + ipAddress + "/AiRProjectMock.php";

//         Create future request
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, future, future);
//        Log.v("Response", urlAdress.getText().toString());

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);

        responseName = (JSONArray) future.get(100, TimeUnit.MILLISECONDS).get("Name");
        responseValue = (JSONArray) future.get(100, TimeUnit.MILLISECONDS).get("Value");
        responseUnit = (JSONArray) future.get(100, TimeUnit.MILLISECONDS).get("Unit");
//        Log.v("Get JSONArray", response.toString());

        View inflatedView = getLayoutInflater().inflate(R.layout.activity_dynamic_list_measurements, null);
        name= (TextView) findViewById(R.id.name);
        value= (TextView) findViewById(R.id.value);
        unit= (TextView) findViewById(R.id.unit);
//
        recyclerView = (RecyclerView)findViewById(R.id.rv_measurements);
        manager = new LinearLayoutManager(this);



        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setLayoutManager(manager);
                try {
                    adapter = new MyRecyclerViewAdapter(toList(responseName), toList(responseValue), toList(responseUnit), R.id.name, R.id.value, R.id.unit);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                recyclerView.setAdapter(adapter);
            }
        });
    }

    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        }  else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
