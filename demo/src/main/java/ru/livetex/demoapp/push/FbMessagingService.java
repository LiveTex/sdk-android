package ru.livetex.demoapp.push;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;

public final class FbMessagingService extends FirebaseMessagingService {

	private static final String TAG = "FbMessagingService";

	@Override
	public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
		Log.d(TAG, "Received push message");

		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		}

		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message notification body: " + remoteMessage.getNotification().getBody());
		}
	}

	@Override
	public void onNewToken(@NonNull String s) {
		Log.i(TAG, "New token = " + s);
		// todo: pass to Livetex
	}
}
