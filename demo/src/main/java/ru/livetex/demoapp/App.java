package ru.livetex.demoapp;

import android.app.Application;

import ru.livetex.sdk.LiveTex;


public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// init LiveTex
		new LiveTex.Builder().build();
	}
}
