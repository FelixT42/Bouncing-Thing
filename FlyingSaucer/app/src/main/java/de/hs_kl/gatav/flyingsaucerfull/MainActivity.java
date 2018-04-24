package de.hs_kl.gatav.flyingsaucerfull;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class MainActivity extends Activity implements SensorEventListener {

    private SpaceGLSurfaceView spaceGLSurfaceView;
    private WindowManager mWindowManager;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Display mDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        spaceGLSurfaceView = new SpaceGLSurfaceView(this);
        spaceGLSurfaceView.context=this;
        setContentView(spaceGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        switch(mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                //Log.d("rot", ""+0);
                spaceGLSurfaceView.setShipVelocity(-event.values[0], 0, -event.values[1]);
                break;
            case Surface.ROTATION_90:
                //Log.d("rot", ""+90);
                spaceGLSurfaceView.setShipVelocity(event.values[1], 0, -event.values[0]);
                break;
            case Surface.ROTATION_180:
                //Log.d("rot", ""+180);
                spaceGLSurfaceView.setShipVelocity(event.values[0], 0, event.values[1]);
                break;
            case Surface.ROTATION_270:
                //Log.d("rot", ""+270);
                spaceGLSurfaceView.setShipVelocity(-event.values[1], 0, event.values[0]);
                break;
        }
    }
}