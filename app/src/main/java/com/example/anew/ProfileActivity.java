package com.example.anew;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;


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


    //For Heart Rate Monitor
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static Camera camera = null;
    private static View image = null;
    private static TextView heartBeatText = null;

    private static PowerManager.WakeLock wakeLock = null;

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



        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOn){
                    isOn = true;
                    BtnStart.setImageResource(R.drawable.ic_stopbtn);
                    logoOff.setImageResource(R.drawable.ic_logoon);
                    chronometer.setVisibility(View.VISIBLE);

                    resetChronometer();
                    startChronometer();
                    numSteps = 0;
                    sensorManager.registerListener(ProfileActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

                }
                else{
                    isOn = false;
                    BtnStart.setImageResource(R.drawable.ic_startbtn);
                    logoOff.setImageResource(R.drawable.ic_profilelogo);

                    stopChronometer();
                    sensorManager.unregisterListener(ProfileActivity.this);

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





}

