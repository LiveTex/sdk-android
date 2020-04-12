package ru.livetex.sdk.network;

public interface AuthConnectionListener {
	void onAuthSuccess(String clientId);
	void onAuthError(Throwable throwable);
}
