package com.example.sensortest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.sensortest.RouteModeClassifier.OnClassifyListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class ModeService extends Service implements SensorEventListener,
		OnConnectionFailedListener, ConnectionCallbacks, OnClassifyListener {
	private static final String TAG = "MyService";
	private static final int NOTIFICATION_ID = 1;
	public static final String ACTION_MODE = "com.example.sensortest.mode";
	private static final String KEY_AVG_ACCEL = "key_avg_accel";
	private static final String KEY_ACCEL_COUNT = "key_accel_count";
	private SensorManager mSensorManager;
	private LocationClient mLocClient;
	private RouteModeClassifier mClassifier;
	private Map<Integer, MediaPlayer> mPlayers;
	private int mCurrentMode = -2;

	private long mAccelCount;
	private float mAvgAccel;

	private static final long LOCATION_INTERVAL = Config.WINDOW_SIZE / 2;

	@Override
	public void onCreate() {
		super.onCreate();
		initVoices();
		initSensors();
		initClassifier();
		initAvgAccel();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mClassifier.start();
		startNotification();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);
		mLocClient.disconnect();
		mClassifier.stop();
		releaseVoices();
	}

	private void initVoices() {
		mPlayers = new HashMap<Integer, MediaPlayer>();
		mPlayers.put(TransMode.Walk.mode(),
				MediaPlayer.create(this, R.raw.walk));
		mPlayers.put(TransMode.Bike.mode(),
				MediaPlayer.create(this, R.raw.bike));
		mPlayers.put(TransMode.Car.mode(), MediaPlayer.create(this, R.raw.car));
		mPlayers.put(TransMode.Run.mode(), MediaPlayer.create(this, R.raw.run));
		mPlayers.put(TransMode.Stay.mode(),
				MediaPlayer.create(this, R.raw.stay));
	}

	private void initClassifier() {
		mClassifier = new RouteModeClassifier(this);
		mClassifier.setOnClassifyListener(this);
	}

	private void initSensors() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mLocClient = new LocationClient(this, this, this);

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mLocClient.connect();
	}

	private void initAvgAccel() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mAvgAccel = prefs.getFloat(KEY_AVG_ACCEL, Config.STANDARD_AVG_ACCEL);
		mAccelCount = prefs.getLong(KEY_ACCEL_COUNT, 0L);
	}

	private void startNotification() {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);
		String msg = "location service is running!";
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		Notification noti = mBuilder.build();
		startForeground(NOTIFICATION_ID, noti);
	}

	@Override
	public void onClassify(final int mode) {
		if (mode == mCurrentMode)
			return;
		mCurrentMode = mode;
		sendMyBroadcast(mode);
		startVoice(mode);
	}

	private void sendMyBroadcast(int mode) {
		Intent intent = new Intent();
		intent.setAction(ACTION_MODE);
		intent.putExtra("mode", mode);
		sendBroadcast(intent);
	}

	private void startVoice(int id) {
		stopVoices();
		try {
			mPlayers.get(id).prepare();
			mPlayers.get(id).start();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}

	}

	private void stopVoices() {
		for (MediaPlayer m : mPlayers.values())
			m.stop();
	}

	private void releaseVoices() {
		for (MediaPlayer m : mPlayers.values())
			m.release();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// standrad the sensor
			float[] values = event.values;
			float accel = 0;
			for (int i = 0; i < 3; i++)
				accel += (values[i] * values[i]);
			accel = (float) Math.sqrt(accel);
			mAvgAccel = (mAccelCount * mAvgAccel + accel) / (++mAccelCount);
			float st = Config.STANDARD_AVG_ACCEL / mAvgAccel;
			for (int i = 0; i < 3; i++) {
				values[i] *= st;
			}
			GSensorPoint p = new GSensorPoint(values);
			mClassifier.getPointRoute().addGSensorPoint(p);
			// every 100 times store the acceleration
			if (mAccelCount % 100 == 0)
				storeAccel(this);
		}
	}

	private synchronized void storeAccel(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putFloat(KEY_AVG_ACCEL, mAvgAccel);
		editor.putLong(KEY_ACCEL_COUNT, mAccelCount);
		editor.commit();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onConnected(Bundle arg0) {
		LocationRequest req = new LocationRequest();
		req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		req.setInterval(LOCATION_INTERVAL);
		req.setFastestInterval(LOCATION_INTERVAL / 2);
		mLocClient.requestLocationUpdates(req, mLocListener);
	}

	@Override
	public void onDisconnected() {
		mLocClient.removeLocationUpdates(mLocListener);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.i(TAG, "onConnectionFailed");
	}

	private LocationListener mLocListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location loc) {
			mClassifier.getPointRoute().addLocation(loc);
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
