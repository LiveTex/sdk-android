package ru.livetex.sdk.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

class NetworkStateObserver {
	enum InternetConnectionStatus {
		CONNECTED,
		DISCONNECTED
	}

	private BehaviorSubject<InternetConnectionStatus> connectionStatusSubject = BehaviorSubject.create();
	private NetworkChangeBroadcastReceiver networkChangeBroadcastReceiver = null;

	Observable<InternetConnectionStatus> status() {
		return connectionStatusSubject;
	}

	@Nullable
	InternetConnectionStatus getStatus() {
		return connectionStatusSubject.getValue();
	}

	void startObserve(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && connectivityManager != null) {
			NetworkChangeCallback callback = new NetworkChangeCallback(connectionStatusSubject, connectivityManager);
			connectivityManager.registerDefaultNetworkCallback(callback);
		} else {
			networkChangeBroadcastReceiver = new NetworkChangeBroadcastReceiver(connectionStatusSubject);
			IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			context.registerReceiver(networkChangeBroadcastReceiver, filter);
		}
	}

	void stopObserve(Context context) {
		if (networkChangeBroadcastReceiver != null) {
			context.unregisterReceiver(networkChangeBroadcastReceiver);
			networkChangeBroadcastReceiver = null;
		}
	}
}