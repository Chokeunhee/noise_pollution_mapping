package com.example.velmurugan.detectnoiseandroidexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.PowerManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.shinelw.library.ColorArcProgressBar;

public class MainActivity extends AppCompatActivity {
    /* constants */
    private static final int POLL_INTERVAL = 300;

    /** running state **/
    private boolean mRunning = false;

    /** config state **/
    private int mThreshold;

    int RECORD_AUDIO = 0;
    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mStatusView,tv_noice, mNoiseStatus;

    /* sound data source */
    private DetectNoise mSensor;
    ColorArcProgressBar bar;
    /****************** Define runnable thread again and again detect noise *********/

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");
            start();
        }
    };

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            updateDisplay("Monitoring Voice...", amp);

            if ((amp > mThreshold)) {
                callForHelp(amp);
                //Log.i("Noise", "==== onCreate ===");
            }
            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };
    /** Called when the activity is first created. */
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Defined SoundLevelView in main.xml file
        setContentView(R.layout.activity_main);
        mStatusView = (TextView) findViewById(R.id.status);
        tv_noice=(TextView)findViewById(R.id.tv_noice);
        mNoiseStatus = findViewById(R.id.noise_status);

        bar=findViewById(R.id.progressBar1);

        // Used to record voice
        mSensor = new DetectNoise();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
    }
    @Override
    public void onResume() {
        super.onResume();
        //Log.i("Noise", "==== onResume ===");

        initializeApplicationConstants();
        if (!mRunning) {
            mRunning = true;
            start();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");
        //Stop noise monitoring
        stop();
    }
    private void start() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }

            //Log.i("Noise", "==== start ===");
        mSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }
    private void stop() {
        Log.d("Noise", "==== Stop Noise Monitoring===");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        bar.setCurrentValues(0);
        updateDisplay("stopped...", 0.0);
        mRunning = false;

    }


    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 8;

    }

    @SuppressLint("SetTextI18n")
    private void updateDisplay(String status, double signalEMA) {
        mStatusView.setText(status);
        //
        if (signalEMA < 1){
            mNoiseStatus.setText("Good");
            mNoiseStatus.setTextColor(Color.GREEN);
        }else{

            mNoiseStatus.setText("Normal");
            mNoiseStatus.setTextColor(Color.YELLOW);
        }
        bar.setCurrentValues((int)signalEMA);
        Log.d("SONUND", String.valueOf(signalEMA));
        tv_noice.setText(signalEMA+"dB");
    }


    @SuppressLint("SetTextI18n")
    private void callForHelp(double signalEMA) {

        //stop();

        // Show alert when noise thersold crossed
        mNoiseStatus.setText("Noisy");
        mNoiseStatus.setTextColor(Color.RED);
        Log.d("SONUND", String.valueOf(signalEMA));
        tv_noice.setText(signalEMA+"dB");
    }

}
