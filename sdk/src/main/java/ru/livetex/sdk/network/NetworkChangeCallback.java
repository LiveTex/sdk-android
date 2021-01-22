package ru.livetex.sdk.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.reactivex.subjects.BehaviorSubject;

class NetworkChangeCallback extends ConnectivityManager.NetworkCallback {
	private final BehaviorSubject<NetworkStateObserver.InternetConnectionStatus> subject;

	@RequiresApi(api = Build.VERSION_CODES.M)
	public NetworkChangeCallback(BehaviorSubject<NetworkStateObserver.InternetConnectionStatus> subject, ConnectivityManager connectivityManager) {
		this.subject = subject;
		Network network= connectivityManager.getActiveNetwork();
		if (network == null) {
			subject.onNext(NetworkStateObserver.InternetConnectionStatus.DISCONNECTED);
		} else {
			NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
			if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
				subject.onNext(NetworkStateObserver.InternetConnectionStatus.CONNECTED);
			} else {
				subject.onNext(NetworkStateObserver.InternetConnectionStatus.DISCONNECTED);
			}
		}
	}

	@Override
	public void onAvailable(@NonNull Network network) {
		super.onAvailable(network);
		subject.onNext(NetworkStateObserver.InternetConnectionStatus.CONNECTED);
	}

	@Override
	public void onLost(@NonNull Network network) {
		super.onLost(network);
		subject.onNext(NetworkStateObserver.InternetConnectionStatus.DISCONNECTED);
	}
}