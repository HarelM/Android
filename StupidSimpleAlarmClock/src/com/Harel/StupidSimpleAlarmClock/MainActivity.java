package com.Harel.StupidSimpleAlarmClock;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int NOTIFICATION_ID = 811984;

	public static final String ALARM_IS_SET = "AlarmIsSet";
	public static final String SNOOZE_LEFT = "SnoozeLeft";
	private static final String HOUR_1 = "Hour1";
	private static final String HOUR_2 = "Hour2";
	private static final String MINUTE_1 = "Minute1";
	private static final String MINUTE_2 = "Minute2";
	
	
	private TextView m_textViewHour1;
	private TextView m_textViewHour2;
	private TextView m_textViewMinute1;
	private TextView m_textViewMinute2;
	private TextView m_textViewSelected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		initAlarmButtons();
		initTextViews();
		initButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(this, AlarmPreferencesActivity.class);
			startActivity(intent);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveSettings();
		super.onSaveInstanceState(outState);
	}

	private void saveSettings() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(HOUR_1, m_textViewHour1.getText().toString());
		editor.putString(HOUR_2, m_textViewHour2.getText().toString());
		editor.putString(MINUTE_1, m_textViewMinute1.getText().toString());
		editor.putString(MINUTE_2, m_textViewMinute2.getText().toString());
		editor.commit();
	}

	private void initAlarmButtons() {

		Button buttonSet = (Button) findViewById(R.id.ButtonSet);
		Button buttonCancel = (Button) findViewById(R.id.ButtonCancel);

		Intent activate = new Intent(MainActivity.this, AlarmReceiver.class);
		final PendingIntent alarmIntent = PendingIntent.getBroadcast(MainActivity.this, 0, activate, 0);
		final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		buttonSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				CharSequence toastText = "";
				alarmManager.cancel(alarmIntent); // cancel previous alarms
				Date alarmTime = getAlarmTime();
				alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTime(), alarmIntent);

				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				long lMillisecondsToAlarm = alarmTime.getTime() - cal.getTimeInMillis();
				int iTotalMinutes = (int) lMillisecondsToAlarm / (1000 * 60);

				toastText = "Alarm set to " + String.valueOf(iTotalMinutes / 60) + " hours and "
						+ String.valueOf(iTotalMinutes % 60) + " Minutes.";

				SharedPreferences.Editor editor = settings.edit();
				editor.putInt(SNOOZE_LEFT, AlarmPreferencesActivity.getSnooze(settings, MainActivity.this) - 1);
				editor.commit();

				saveSettings();
				MainActivity.enableNotificationIcon(true, MainActivity.this, alarmTime);

				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				boolean bCloseOnSet = settings.getBoolean(MainActivity.this.getString(R.string.PreferenceCloseOnSetKey), false);
				if (bCloseOnSet == true) {
					finish();
				}
			}
		});

		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				CharSequence toastText = "";
				alarmManager.cancel(alarmIntent);
				toastText = "Alarm canceled.";
				MainActivity.enableNotificationIcon(false, MainActivity.this, new Date());
				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				
				boolean bCloseOnCancel = settings.getBoolean(MainActivity.this.getString(R.string.PreferenceCloseOnCancelKey), false);
				if (bCloseOnCancel == true) {
					finish();
				}
			}
		});
	}

	private void initTextViews() {

		m_textViewHour1 = (TextView) findViewById(R.id.textViewHour1);
		m_textViewHour2 = (TextView) findViewById(R.id.textViewHour2);
		m_textViewMinute1 = (TextView) findViewById(R.id.textViewMinute1);
		m_textViewMinute2 = (TextView) findViewById(R.id.textViewMinute2);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean bUseDefaultTime = settings.getBoolean(getString(R.string.PreferenceUseDefaultTimeKey), false);
		Boolean bAlarmIsSet = settings.getBoolean(ALARM_IS_SET, false);
		if (bUseDefaultTime == true && bAlarmIsSet == false)
		{
			String sDefaultTime = settings.getString(getString(R.string.PreferenceDefaultTimeKey), getString(R.string.DefaultTime));
			int iHour = TimePreference.getHour(sDefaultTime);
			int iMinute = TimePreference.getMinute(sDefaultTime);
			m_textViewHour1.setText(String.valueOf(iHour / 10));
			m_textViewHour2.setText(String.valueOf(iHour % 10));
			m_textViewMinute1.setText(String.valueOf(iMinute / 10));
			m_textViewMinute2.setText(String.valueOf(iMinute % 10));
		}
		else
		{
			m_textViewHour1.setText(settings.getString(HOUR_1, "0"));
			m_textViewHour2.setText(settings.getString(HOUR_2, "8"));
			m_textViewMinute1.setText(settings.getString(MINUTE_1, "0"));
			m_textViewMinute2.setText(settings.getString(MINUTE_2, "0"));
		}
		m_textViewSelected = m_textViewHour1;

		OnTouchListener textViewOnTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				view.requestFocus();
				m_textViewSelected = (TextView) view;
				return false;
			}
		};

		m_textViewHour1.setOnTouchListener(textViewOnTouchListener);
		m_textViewHour2.setOnTouchListener(textViewOnTouchListener);
		m_textViewMinute1.setOnTouchListener(textViewOnTouchListener);
		m_textViewMinute2.setOnTouchListener(textViewOnTouchListener);

		m_textViewHour1.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int iHours = -1;
				try {
					iHours = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					Log.d("StupidSimpleAlarmClock", e.getMessage() + "\n" + e.getStackTrace());
				}

				if (iHours > 2) {
					m_textViewHour1.setText("2");
					m_textViewHour2.setText("3");
				} else if (iHours < 0) {
					m_textViewHour1.setText("0");
				}
				m_textViewHour2.requestFocus();
				m_textViewSelected = m_textViewHour2;
			}
		});

		m_textViewHour2.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int iHours = -1;
				try {
					iHours = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					Log.d("StupidSimpleAlarmClock", e.getMessage() + "\n" + e.getStackTrace());
				}
				if (m_textViewHour1.getText().toString().equals("2") && iHours > 3) {
					m_textViewHour2.setText("3");
				} else if (iHours < 0) {
					m_textViewHour1.setText("0");
				}
				m_textViewMinute1.requestFocus();
				m_textViewSelected = m_textViewMinute1;
			}
		});

		m_textViewMinute1.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int iMinute = -1;
				try {
					iMinute = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					Log.d("StupidSimpleAlarmClock", e.getMessage() + "\n" + e.getStackTrace());
				}

				if (iMinute > 5) {
					m_textViewMinute1.setText("5");
					m_textViewMinute2.setText("9");
				} else if (iMinute < 0) {
					m_textViewMinute1.setText("0");
				}
				m_textViewMinute2.requestFocus();
				m_textViewSelected = m_textViewMinute2;
			}
		});

		m_textViewMinute2.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int iMinute = -1;
				try {
					iMinute = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					Log.d("StupidSimpleAlarmClock", e.getMessage() + "\n" + e.getStackTrace());
				}
				if (iMinute < 0) {
					m_textViewMinute2.setText("0");
				}
				m_textViewHour1.requestFocus();
				m_textViewSelected = m_textViewHour1;
			}
		});
	}

	private void initButtons() {
		Button[] arButtons = new Button[] { (Button) findViewById(R.id.Button0), (Button) findViewById(R.id.Button1),
				(Button) findViewById(R.id.Button2), (Button) findViewById(R.id.Button3),
				(Button) findViewById(R.id.Button4), (Button) findViewById(R.id.Button5),
				(Button) findViewById(R.id.Button6), (Button) findViewById(R.id.Button7),
				(Button) findViewById(R.id.Button8), (Button) findViewById(R.id.Button9), };

		for (int iButton = 0; iButton < arButtons.length; iButton++) {
			arButtons[iButton].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Button buttonClicked = (Button) view;
					m_textViewSelected.setText(buttonClicked.getText().toString());
				}
			});
		}
		Button[] arAdvanveButtons = new Button[] { (Button) findViewById(R.id.Button00),
				(Button) findViewById(R.id.Button15), (Button) findViewById(R.id.Button30),
				(Button) findViewById(R.id.Button45), };
		for (int iButton = 0; iButton < arAdvanveButtons.length; iButton++) {
			if (arAdvanveButtons[iButton] == null) {
				// this happens in landscape mode
				continue;
			}
			arAdvanveButtons[iButton].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Button buttonClicked = (Button) view;
					CharSequence cs = buttonClicked.getText();
					m_textViewMinute1.setText(cs.subSequence(0, 1));
					m_textViewMinute2.setText(cs.subSequence(1, 2));
				}
			});
		}
	}

	private Date getAlarmTime() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();

		int iHours = Integer.parseInt(m_textViewHour1.getText().toString() + m_textViewHour2.getText().toString());
		int iMinutes = Integer
				.parseInt(m_textViewMinute1.getText().toString() + m_textViewMinute2.getText().toString());

		date.setHours(iHours);
		date.setMinutes(iMinutes);
		date.setSeconds(0); // HM for debug - remove this

		if (date.before(calendar.getTime())) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			date = calendar.getTime();
			date.setHours(iHours);
			date.setMinutes(iMinutes);
		}

		return date;
	}

	public static void enableNotificationIcon(boolean bEnable, Context context, Date dateAlarm) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bEnabledInSettings = settings.getBoolean(context.getString(R.string.PreferenceShowNotificationIconKey), true);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(ALARM_IS_SET, bEnable);
		editor.commit();
		
		if (bEnable == false || bEnabledInSettings == false) {
			notificationManager.cancel(NOTIFICATION_ID);
			return;
		}
		String sAlarmTime = "Alarm is set to: " + String.format("%02d", dateAlarm.getHours())
				+ context.getString(R.string.colon) + String.format("%02d", dateAlarm.getMinutes());
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Alarm Clock")
				.setContentText(sAlarmTime)
				.setOngoing(true);
		// Creates an explicit intent for an Activity in your application
		Intent resultIntent = new Intent(context, MainActivity.class);

		// The stack builder object will contain an artificial back stack
		// for the started Activity.
		// This ensures that navigating backward from the Activity leads out
		// of your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
}
