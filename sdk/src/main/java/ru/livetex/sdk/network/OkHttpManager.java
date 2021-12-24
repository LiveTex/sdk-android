package ru.livetex.sdk.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.WorkerThread;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;

final class OkHttpManager {
	private static final long CONNECT_TIMEOUT_SECONDS = 30;
	private static final long READ_TIMEOUT_SECONDS = 60;
	private static final long WRITE_TIMEOUT_SECONDS = 30;

	private static final long WEBSOCKET_PING_INTERVAL = 10_000L;

	private final okhttp3.OkHttpClient client;
	private final okhttp3.OkHttpClient webSocketClient;

	OkHttpManager(boolean isNetworkLoggingEnabled) {
		final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(isNetworkLoggingEnabled ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.addInterceptor(loggingInterceptor);
		builder.connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		builder.readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		builder.writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		client = builder.build();

		builder.pingInterval(WEBSOCKET_PING_INTERVAL, TimeUnit.MILLISECONDS);
		webSocketClient = builder.build();
	}

	OkHttpClient getClient() {
		return client;
	}

	@WorkerThread
	String requestString(Request request) throws IOException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
		builder.url(request.url());
		builder.headers(request.headers());
		builder.method(request.method(), request.body());

		okhttp3.Request okRequest = builder.build();

		Response response = client.newCall(okRequest).execute();
		if (!response.isSuccessful()) {
			throw new IOException("Unexpected code " + response.code() + ", message " + response.message());
		}
		return response.body().string();
	}

	WebSocket webSocketConnection(Request request, WebSocketListener listener) {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
		builder.url(request.url());
		builder.headers(request.headers());
		builder.method(request.method(), request.body());

		okhttp3.Request okRequest = builder.build();

		return webSocketClient.newWebSocket(okRequest, listener);
	}
}
