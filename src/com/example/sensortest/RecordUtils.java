package com.example.sensortest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class RecordUtils {
	public static RecordUtils instance;

	public static RecordUtils getInstance(Context context) {
		if (instance == null) {
			instance = new RecordUtils();
		}
		return instance;
	}

	private long preTime = 0;
	private static long VALID_TIME_INTERVAL = 50;

	@SuppressLint({ "NewApi", "SimpleDateFormat" })
	public synchronized void recordLocation(Location curLocation) {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/myLocation";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		File records = new File(path + "/record_location.txt");
		try {
			FileOutputStream out = new FileOutputStream(records, true);
			SimpleDateFormat dateformat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String timeStr = dateformat.format(new Date(curLocation.getTime()));
			String str = timeStr + "," + curLocation.getTime() + ","
					+ curLocation.getLatitude() + ","
					+ curLocation.getLongitude() + ","
					+ curLocation.getProvider() + "," + curLocation.getSpeed()
					+ "\n";
			Log.i("LLLLLLLLLLLLLLL", str);
			out.write(str.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint({ "NewApi", "SimpleDateFormat" })
	public synchronized void recordSensor(SensorEvent event) {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/myLocation";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		File records = new File(path + "/record_sensor.txt");
		try {
			long minSeconds = event.timestamp / 1000000;
			if (!isInValidTime(minSeconds))
				return;
			preTime = minSeconds;
			FileOutputStream out = new FileOutputStream(records, true);
			SimpleDateFormat dateformat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String timeStr = dateformat.format(new Date(minSeconds));
			String str = timeStr + "," + (minSeconds) + "," + event.values[0]
					+ "," + event.values[1] + "," + event.values[2] + "\n";
			out.write(str.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isInValidTime(long cur) {
		return cur - preTime >= VALID_TIME_INTERVAL;
	}
}
