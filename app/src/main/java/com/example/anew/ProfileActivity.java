package com.example.anew;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.anew.MeasureActivity.c;


public class ProfileActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    //For Pedometer
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps;
    private TextView TvSteps;
    private TextView TvCalories;
    private TextView TvDist;
    private ImageButton BtnStart;
    private ImageView logoOff;
    private Chronometer chronometer;
    boolean isOn = false;
    private boolean running;
    private long pauseOffset;
    private Button heartButton;



    //For Heart Rate Monitor
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static Camera camera = null;
    private static View image = null;
    private static TextView heartBeatText = null;

    private static PowerManager.WakeLock wakeLock = null;
    private String number;

    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];

    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Anton-Regular.ttf");
        number = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("LAST_MEASURE", "0");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = findViewById(R.id.tvSteps);
        TvCalories = findViewById(R.id.TvCalories);
        TvDist = findViewById(R.id.TvDist);
        BtnStart = findViewById(R.id.BtnStart);
        logoOff = findViewById(R.id.logoOff);
        chronometer = findViewById(R.id.chronometer);
        chronometer.setTypeface(font, Typeface.NORMAL);

        heartBeatText = findViewById(R.id.heartBeatText);

        heartBeatText.setText(number);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }



        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOn){
                    isOn = true;
                    BtnStart.setImageResource(R.drawable.ic_stopbtn);
                    logoOff.setImageResource(R.drawable.ic_logoon);
                    chronometer.setVisibility(View.VISIBLE);
                    findViewById(R.id.heart_button).setEnabled(false);
                    resetChronometer();
                    startChronometer();
                    numSteps = 0;
                    sensorManager.registerListener(ProfileActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

                }
                else{
                    isOn = false;
                    BtnStart.setImageResource(R.drawable.ic_startbtn);
                    logoOff.setImageResource(R.drawable.ic_profilelogo);
                    findViewById(R.id.heart_button).setEnabled(true);
                    stopChronometer();
                    sensorManager.unregisterListener(ProfileActivity.this);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Steps", TvSteps.getText().toString());
                    editor.putString("Calories", TvCalories.getText().toString());
                    editor.putString("Distance", TvDist.getText().toString());
                    editor.apply();

                }

            }
        });




        //Heart Rate
        heartBeatText = findViewById(R.id.heartBeatText);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK ,":DoNotDimScreen");


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long timeNs) {
        DecimalFormat df2 = new DecimalFormat("#.###");
        numSteps++;
        double calories = 0.04 * ((double)numSteps);
        double distance = 0.8 * ((double)numSteps);

        TvSteps.setText(""+numSteps);
        TvCalories.setText(""+df2.format(calories));
        TvDist.setText(""+df2.format(distance)+" m");



    }

    public void startChronometer(){
        if(!running){
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }
    public void stopChronometer(){
        if(running){
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }
    public void resetChronometer(){
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    public void resetFunction(View view){
        chronometer.setText("00:00");
        TvDist.setText("- -");
        TvSteps.setText("- -");
        TvCalories.setText("- -");
    }


    public void heart(View view){
        findViewById(R.id.heart_button).setEnabled(false);
        Intent intent = new Intent(ProfileActivity.this, MeasureActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String number = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("LAST_MEASURE", "0");
        heartBeatText.setText(number);
        findViewById(R.id.heart_button).setEnabled(true);
//        StringRequest request = new StringRequest(Request.Method.POST, Config.upload, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        })
//        {
//
//        };



        TvSteps.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("Steps", "0"));
        TvCalories.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("Calories", "0"));
        TvDist.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("Distance", "0m"));
    }
}

