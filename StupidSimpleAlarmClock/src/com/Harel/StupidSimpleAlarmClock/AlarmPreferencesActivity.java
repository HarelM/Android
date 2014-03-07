package com.Harel.StupidSimpleAlarmClock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class AlarmPreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private EditTextPreference m_EditTextPreferenceSnooze;
	private EditTextPreference m_EditTextPreferenceRepeat;
	private TimePreference m_TimePreferenceDefaultTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		m_EditTextPreferenceRepeat = (EditTextPreference)getPreferenceScreen()
				.findPreference(getString(R.string.PreferenceRepeat));
		m_EditTextPreferenceSnooze = (EditTextPreference)getPreferenceScreen()
				.findPreference(getString(R.string.PreferenceSnooze));
		m_TimePreferenceDefaultTime = (TimePreference)getPreferenceScreen()
				.findPreference(getString(R.string.PreferenceDefaultTimeKey));
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup the initial values
		SharedPreferences settings = getPreferenceScreen().getSharedPreferences();
		onSharedPreferenceChanged(settings, getString(R.string.PreferenceSnooze));
		onSharedPreferenceChanged(settings, getString(R.string.PreferenceRepeat));
		onSharedPreferenceChanged(settings, getString(R.string.PreferenceDefaultTimeKey));
		
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences settings, String key) {
		// Let's do something a preference value changes
		if (key.equals(getString(R.string.PreferenceRepeat))) 
		{
			m_EditTextPreferenceRepeat.setSummary(getString(R.string.PreferenceRepeatSummary) + "\n"
					+ getString(R.string.PreferenceCurrentSummary) + " "
					+ String.valueOf(AlarmPreferencesActivity.getRepeat(settings, this)));
		} 
		else if (key.equals(getString(R.string.PreferenceSnooze))) 
		{
			m_EditTextPreferenceSnooze.setSummary(getString(R.string.PreferenceSnoozeSummary) + "\n"
					+ getString(R.string.PreferenceCurrentSummary) + " "
					+ String.valueOf(AlarmPreferencesActivity.getSnooze(settings, this)));
		}
		else if (key.equals(getString(R.string.PreferenceDefaultTimeKey))) 
		{
			m_TimePreferenceDefaultTime.setSummary(getString(R.string.PreferenceDefaultTimeSummary) + "\n"
					+ getString(R.string.PreferenceCurrentSummary) + " "
					+ settings.getString(getString(R.string.PreferenceDefaultTimeKey), getString(R.string.DefaultTime)));
		}	
	}

	public static int getSnooze(SharedPreferences settings, Context context) {
		try {
			return Integer.parseInt(settings.getString(context.getString(R.string.PreferenceSnooze), "10"));
		} catch (NumberFormatException e) {
			return 10;
		}

	}

	public static int getRepeat(SharedPreferences settings, Context context) {
		try {
			return Integer.parseInt(settings.getString(context.getString(R.string.PreferenceRepeat), "3"));
		} catch (NumberFormatException e) {
			return 3;
		}

	}
}
