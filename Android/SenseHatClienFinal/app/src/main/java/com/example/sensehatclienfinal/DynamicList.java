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

    private Button buttonChart;
    int temp;
    RequestQueue queue;
    JSONArray responseName;
    JSONArray responseValue;
    JSONArray responseUnit;
    EditText urlAdressList;
    TextView name;
    TextView value;
    TextView unit;
    Timer timerList;
    int kList = 0; //!< Samples counter
    float temperatureValue;
    float humidityValue;
    float pressureValue;
    String temperatureName;
    String humidityName;
    String pressureName;
    String temperatureUnit;
    String humidityUnit;
    String pressureUnit;
    LinearLayoutManager manager;
    List listName = new ArrayList();
    List listValues = new ArrayList();
    List listUnit = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_list);

        urlAdressList= (EditText) findViewById(R.id.urlAdressList);

//        buttonChart = (Button) findViewById(R.id.buttonGoToChart);

//        setContentView(R.layout.activity_dynamic_list_measurements);

//        name= (TextView) findViewById(R.id.name);


    }

    public void goToChart(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        if(timerList != null) {
            timerList.cancel();
            timerList = null;
        }
    }

    public void goToLED(View v){
        Intent intent = new Intent(this, LEDSending.class);
        startActivity(intent);

        if(timerList != null) {
            timerList.cancel();
            timerList = null;
        }
    }

    public void refresh(View v){

        if(timerList == null) {
            kList = 0;

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
            timerList.scheduleAtFixedRate(filterTimerTask, 0, (int)(100));
        }
    }

    public void server() throws ExecutionException, InterruptedException, TimeoutException, JSONException {
        queue = Volley.newRequestQueue(this.getApplicationContext());

//         Create future request
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, urlAdressList.getText().toString(), null, future, future);
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
