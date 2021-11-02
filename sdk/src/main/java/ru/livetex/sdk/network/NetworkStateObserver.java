package ru.livetex.sdk.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class NetworkStateObserver {
	public enum InternetConnectionStatus {
		CONNECTED,
		DISCONNECTED
	}

	private final BehaviorSubject<InternetConnectionStatus> connectionStatusSubject = BehaviorSubject.create();
	private NetworkChangeBroadcastReceiver networkChangeBroadcastReceiver = null;

	public Observable<InternetConnectionStatus> status() {
		return connectionStatusSubject;
	}

	@Nullable
	public InternetConnectionStatus getStatus() {
		return connectionStatusSubject.getValue();
	}

	public void startObserve(Context context) {
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

	public void stopObserve(Context context) {
		if (networkChangeBroadcastReceiver != null) {
			context.unregisterReceiver(networkChangeBroadcastReceiver);
			networkChangeBroadcastReceiver = null;
		}
	}
}