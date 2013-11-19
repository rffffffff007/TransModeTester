package com.example.sensortest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private BroadcastReceiver mReceiver = null;
	private TextView tvStatus = null;
	private Button btnStart;
	private Button btnStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvStatus = (TextView) findViewById(R.id.text);
		btnStart = (Button) findViewById(R.id.btn_start);
		btnStop = (Button) findViewById(R.id.btn_stop);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(ModeService.ACTION_MODE)) {
					int mode = intent.getIntExtra("mode", 0);
					String modeText = TransMode.getTransMode(mode).name();
					Log.i(TAG, "Mode is " + modeText);
					tvStatus.setText("your status is: " + modeText);
				}
			}
		};
		registerReceiver(mReceiver, new IntentFilter(ModeService.ACTION_MODE));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start:
			startService(new Intent(this, ModeService.class));
			tvStatus.setText("service is start!");
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			break;
		case R.id.btn_stop:
			stopService(new Intent(this, ModeService.class));
			tvStatus.setText("service is stop!");
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			break;
		}
	}

}
