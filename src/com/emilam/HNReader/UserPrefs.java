package com.emilam.HNReader;

import android.os.Bundle;
import android.preference.PreferenceActivity;



public class UserPrefs extends PreferenceActivity {

	public static final String INSTAPAPER_USERNAME = "instapaper_username";
	public static final String INSTAPAPER_PASSWORD = "instapaper_password";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}

}

