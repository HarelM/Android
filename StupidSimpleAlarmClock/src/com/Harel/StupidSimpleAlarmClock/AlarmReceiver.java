package com.Harel.StupidSimpleAlarmClock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// start the snooze/stop GUI

		Intent intentAlarm = new Intent(context, AlarmActivity.class);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intentAlarm);
	}
}
