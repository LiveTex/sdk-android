package ru.livetex.sdk.network;

import android.util.Log;

import okio.ByteString;

// todo: interface
public class LiveTexMessageProcessor {
	public void onMessage(String text) {
		Log.e("LiveTexMessageProcessor", "onMessage " + text);
	}

	public void onDataMessage(ByteString bytes) {

	}
}
