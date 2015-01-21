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
	
	private static final String POSITION = "Position";
	private static final String ORIGINAL_VOLUME = "OriginalVolume";
	
	Button m_buttonSnooze;
	Button m_buttonStop;
	TextView m_textViewCurrentTime;

	private MediaPlayer m_MediaPlayer;
	private int m_iPosition;
	private int m_iOriginalAlarmVolume;

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
		m_iOriginalAlarmVolume = -1;
		m_MediaPlayer = null;
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
		outState.putInt(POSITION, m_iPosition);
		outState.putInt(ORIGINAL_VOLUME, m_iOriginalAlarmVolume);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		m_iPosition = savedInstanceState.getInt(POSITION);
		m_iOriginalAlarmVolume = savedInstanceState.getInt(ORIGINAL_VOLUME);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		if (m_iOriginalAlarmVolume != -1)
		{
			final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, m_iOriginalAlarmVolume, 0);
		}
		stopAndReleaseMediaPlayer();
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
				int iSnoozeLeft = settings.getInt(MainActivity.SNOOZE_LEFT, 1);
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
					editor.putInt(MainActivity.SNOOZE_LEFT, iSnoozeLeft - 1); // disabling
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
		String str = getString(R.string.PreferenceAlarmNoiseKey);
		String alarms = getAlarms.getString(str, android.provider.Settings.System.DEFAULT_RINGTONE_URI.toString());
		Uri uri = Uri.parse(alarms);
		stopAndReleaseMediaPlayer();
		m_MediaPlayer = new MediaPlayer();
		try {
			m_MediaPlayer.setDataSource(this, uri);
			final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			int iVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
			if (iVolume <= 0)
			{
				// if we are on silent mode, we'll change the volume and revert when finished. 
				m_iOriginalAlarmVolume = iVolume;
				audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 
						audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 
						0);
			} 
			m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);	
			m_MediaPlayer.prepare();
			m_MediaPlayer.setLooping(true);
		} catch (IOException e) {
			Log.d("StupidSimpleAlarmClock", "Problem playing the selected sound");
		}
	}
	
	private void stopAndReleaseMediaPlayer()
	{
		if (m_MediaPlayer == null)
		{
			return;
		}
		if (m_MediaPlayer.isPlaying() == true) {
			m_MediaPlayer.stop();
		}
		m_MediaPlayer.release();
		m_MediaPlayer = null;
	}
}
