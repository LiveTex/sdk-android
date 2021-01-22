package ru.livetex.sdk.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import io.reactivex.subjects.BehaviorSubject;

class NetworkChangeBroadcastReceiver extends BroadcastReceiver {
	private final BehaviorSubject<NetworkStateObserver.InternetConnectionStatus> subject;

	NetworkChangeBroadcastReceiver(BehaviorSubject<NetworkStateObserver.InternetConnectionStatus> subject) {
		this.subject = subject;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean noConnectivity  = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (noConnectivity) {
			subject.onNext(NetworkStateObserver.InternetConnectionStatus.DISCONNECTED);
		} else {
			subject.onNext(NetworkStateObserver.InternetConnectionStatus.CONNECTED);
		}
	}
}