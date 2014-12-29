package com.adrielservice.gia.callrecorder;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class CallRecorder extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setDefaultTab(0);

		TabHost tabHost = getTabHost();

		TabSpec tab1 = tabHost.newTabSpec("Call Log");
		tab1.setContent(new Intent(this, CallLog.class));
		tab1.setIndicator("Call Log");

        TabSpec tab2 = tabHost.newTabSpec("Preferences");
        tab2.setContent(new Intent(this, Preferences.class));
        tab2.setIndicator("Preferences");
		
		tabHost.addTab(tab1);
		tabHost.addTab(tab2);
	}

	public static class MyTabIndicator extends LinearLayout {
		public MyTabIndicator(Context context, String label) {
			super(context);

			View tab = LayoutInflater.from(context).inflate(R.layout.tab_indicator, this);

			TextView tv = (TextView) tab.findViewById(R.id.tab_label);
			tv.setText(label);
		}
	}
}
