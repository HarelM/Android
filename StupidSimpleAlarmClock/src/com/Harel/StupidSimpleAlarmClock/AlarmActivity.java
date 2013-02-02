package com.Harel.StupidSimpleAlarmClock;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends Activity {
	Button m_buttonSnooze;
	Button m_buttonStop;
	TextView m_textViewCurrentTime;

	private MediaPlayer m_MediaPlayer;
	private int m_iPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_activity);

		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		initCurrentTime();
		initButtons();

		m_iPosition = 0;
	}

	@Override
	protected void onResume() {
		prepareSound();
		if (m_iPosition != 0) {
			m_MediaPlayer.seekTo(m_iPosition);
		}
		m_MediaPlayer.start();
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		m_MediaPlayer.pause();
		m_iPosition = m_MediaPlayer.getCurrentPosition();
		outState.putInt("Position", m_iPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		m_iPosition = savedInstanceState.getInt("Position");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		if (m_MediaPlayer.isPlaying() == true) {
			m_MediaPlayer.stop();
		}
		super.onDestroy();
	}

	private void initCurrentTime() {
		m_textViewCurrentTime = (TextView) findViewById(R.id.textViewCurrentTime);
		Calendar calendar = Calendar.getInstance();
		m_textViewCurrentTime.setText(String.format("%02d", calendar.getTime().getHours()) + getString(R.string.colon)
				+ String.format("%02d", calendar.getTime().getMinutes()));
	}

	private void initButtons() {
		m_buttonSnooze = (Button) findViewById(R.id.buttonSnooze);
		m_buttonSnooze.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				int iSnoozeLeft = settings.getInt("SnoozeLeft", 1);
				MainActivity.enableNotificationIcon(false, AlarmActivity.this, new Date());
				if (iSnoozeLeft != 0) {
					Intent activate = new Intent(AlarmActivity.this, AlarmReceiver.class);
					PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmActivity.this, 0, activate, 0);
					AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

					int iMinutes = AlarmPreferencesActivity.getSnooze(settings, AlarmActivity.this);
					Calendar calendar = Calendar.getInstance();
					calendar.roll(Calendar.MINUTE, iMinutes);
					alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

					SharedPreferences.Editor editor = settings.edit();
					editor.putInt("SnoozeLeft", iSnoozeLeft - 1); // disabling
					// Commit the edits!
					editor.commit();

					// add snooze time of the alarm in the notification area
					MainActivity.enableNotificationIcon(true, AlarmActivity.this, calendar.getTime());
				}

				m_MediaPlayer.stop();
				finish();
			}
		});

		m_buttonStop = (Button) findViewById(R.id.buttonStop);
		m_buttonStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				m_MediaPlayer.stop();
				MainActivity.enableNotificationIcon(false, AlarmActivity.this, new Date());
				finish();
			}
		});
	}

	private void prepareSound() {
		SharedPreferences getAlarms = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String alarms = getAlarms.getString("Audio", "default ringtone");
		Uri uri = Uri.parse(alarms);
		m_MediaPlayer = new MediaPlayer();
		try {
			m_MediaPlayer.setDataSource(this, uri);
			final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				m_MediaPlayer.prepare();
				m_MediaPlayer.setLooping(true);
			}
		} catch (IOException e) {
			Log.d("StupidSimpleAlarmClock", "Problem playing the selected sound");
		}
	}
}
