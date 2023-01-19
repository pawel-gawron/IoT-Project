package com.example.sensehatclienfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LEDSending extends AppCompatActivity {

    SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    View colorView;     ///< Color preview
    EditText urlText;
    String url = "http://192.168.1.208/led_display.php";  ///< Default IoT server script URL
    private RequestQueue queue; ///< HTTP requests queue

    /* BEGIN colors */
    int ledActiveColorA; ///< Active color Alpha components
    int ledActiveColorR; ///< Active color Red components
    int ledActiveColorG; ///< Active color Green components
    int ledActiveColorB; ///< Active color Blue components

    int ledActiveColor;  ///< Active color in Int ARGB format

    int ledOffColor;       ///< 'LED-is-off' color in Int ARGB format
    Vector<Integer> ledOffColorVec; ///< 'LED-is-off' color in Int ARGB format

    Integer[][][] ledDisplayModel = new Integer[8][8][3]; ///< LED display data model
    /* BEGIN colors */

    Map<String, String> paramsClear = new HashMap<String, String>(); ///< HTTP POST data: clear display command

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_sending);

        ledOffColor = ResourcesCompat.getColor(getResources(), R.color.ledIndBackground, null);
        ledOffColorVec = intToRgb(ledOffColor);

        ledActiveColor = ledOffColor;

        ledActiveColorR = 0x00;
        ledActiveColorG = 0x00;
        ledActiveColorB = 0x00;

        clearDisplayModel();

        redSeekBar = (SeekBar)findViewById(R.id.seekBarR);
        redSeekBar.setMax(255);
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('R', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        greenSeekBar = (SeekBar)findViewById(R.id.seekBarG);
        greenSeekBar.setMax(255);
        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('G', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        blueSeekBar = (SeekBar)findViewById(R.id.seekBarB);
        blueSeekBar.setMax(255);
        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('B', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        colorView = findViewById(R.id.colorView);

        urlText = findViewById(R.id.urlText);
        urlText.setText(url);

        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // "LEDij" : "[i,j,r,g,b]"
                String data ="["+Integer.toString(i)+","+Integer.toString(j)+",0,0,0]";
                paramsClear.put(ledIndexToTag(i, j), data);
            }
        }
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


    public void clearDisplayModel() {
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledDisplayModel[i][j][0] = 0;
                ledDisplayModel[i][j][1] = 0;
                ledDisplayModel[i][j][2] = 0;
            }
        }
    }

    public Vector<Integer> intToRgb(int argb) {
        int _r = (argb >> 16) & 0xff;
        int _g = (argb >> 8) & 0xff;
        int _b = argb & 0xff;
        Vector<Integer> rgb = new Vector<>(3);
        rgb.add(0,_r);
        rgb.add(1,_g);
        rgb.add(2,_b);
        return rgb;
    }

    int seekBarUpdate(char color, int value) {
        switch(color) {
            case 'R': ledActiveColorR = value; break;
            case 'G': ledActiveColorG = value; break;
            case 'B': ledActiveColorB = value; break;
            default: /* Do nothing */ break;
        }
        ledActiveColorA = (ledActiveColorR+ledActiveColorG+ledActiveColorB)/3;
        return argbToInt(ledActiveColorA,  ledActiveColorR, ledActiveColorG, ledActiveColorB);
    }

    public int argbToInt(int _a, int _r, int _g, int _b){
        return  (_a & 0xff) << 24 | (_r & 0xff) << 16 | (_g & 0xff) << 8 | (_b & 0xff);
    }

    String ledIndexRowToTag(int y) {
        return Integer.toString(y);
    }
    String ledIndexColumnToTag(int x) {
        return Integer.toString(x);
    }

    String ledIndexToTag(int x, int y) {
        return "LED" + Integer.toString(x) + Integer.toString(y);
    }

    Vector<Integer> ledTagToIndex(String tag) {
        // Tag: 'LEDxy"
        Vector<Integer> vec = new Vector<>(2);
        vec.add(0, Character.getNumericValue(tag.charAt(3)));
        vec.add(1, Character.getNumericValue(tag.charAt(4)));
        return vec;
    }

    JSONObject ledIndexToJsonData(int x, int y) throws JSONException {
        JSONObject ledColors = new JSONObject();
        Integer _r = ledDisplayModel[x][y][0];
        Integer _g = ledDisplayModel[x][y][1];
        Integer _b = ledDisplayModel[x][y][2];
        ledColors.put("R", _r);
        ledColors.put("G", _g);
        ledColors.put("B", _b);
        if(_r == 0 && _g == 0 && _b == 0) ledColors.put("state", false); else ledColors.put("state", true);
        return ledColors;
    }

    boolean ledColorNotNull(int x, int y) {
        return !((ledDisplayModel[x][y][0]==0)||(ledDisplayModel[x][y][1]==0)||(ledDisplayModel[x][y][2]==0));
    }

    public void changeLedIndicatorColor(View v) {
        // Set active color as background
        v.setBackgroundColor(ledActiveColor);
        // Find element x-y position
        String tag = (String)v.getTag();
        Vector<Integer> index = ledTagToIndex(tag);
        int x = (int)index.get(0);
        int y = (int)index.get(1);
        // Update LED display data model
        ledDisplayModel[x][y][0] = ledActiveColorR;
        ledDisplayModel[x][y][1] = ledActiveColorG;
        ledDisplayModel[x][y][2] = ledActiveColorB;
    }


    public JSONObject  getDisplayControlParams() throws JSONException {
        String ledColumn;
        String ledRow;
        JSONObject position_color_data;
        JSONObject ledParams = new JSONObject();
        Map<String, String>  params = new HashMap<String, String>();
        for(int i = 0; i < 8; i++) { //Column
            JSONObject ledParamsTemporary = new JSONObject();
            ledColumn = ledIndexColumnToTag(i);
            for (int j = 0; j < 8; j++) { //Row
                ledRow = ledIndexRowToTag(j);
                position_color_data = ledIndexToJsonData(i, j);
                ledParamsTemporary.put(ledRow, position_color_data);
//                Log.v("Color Data", String.valueOf(ledParamsTemporary));
            }
            ledParams.put(ledColumn, ledParamsTemporary);
        }
//        Log.v("Params", String.valueOf(ledParams));
        return ledParams;
    }


    public void clearAllLed(View v) {
        // Clear LED display GUI
        TableLayout tb = (TableLayout)findViewById(R.id.ledTable);
        View ledInd;
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledInd = tb.findViewWithTag(ledIndexToTag(i, j));
                ledInd.setBackgroundColor(ledOffColor);
            }
        }

        // Clear LED display data model
        clearDisplayModel();

        // Clear physical LED display
//        sendClearRequest();
    }

    public void sendControlRequest(View v) throws JSONException {
        queue = Volley.newRequestQueue(this.getApplicationContext());
        url = urlText.getText().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, getDisplayControlParams(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Response: "+response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjectRequest);
    }

}
