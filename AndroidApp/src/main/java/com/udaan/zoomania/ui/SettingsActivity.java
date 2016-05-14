package com.udaan.zoomania.ui;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFragmentManager()
			.beginTransaction()
			.replace(android.R.id.content, new PrefsFragment())
			.commit();
	}
	
	public static class PrefsFragment extends PreferenceFragment {
		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.activity_settings);
        }
	}
}
