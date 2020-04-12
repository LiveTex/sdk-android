package ru.livetex.sdk.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.WebSocket;

public final class NetworkManager {
	private static final String TAG = "NetworkManager";
	private static NetworkManager instance = new NetworkManager();

	private static final String HOST = "sdk-mock.livetex.ru//"; // todo: dynamic or not here
	private static final String HOST_API = "http://" + HOST + "v1/"; // todo: https
	private static final String HOST_WS = "ws://" + HOST + "v1/ws/{clientId}"; // todo: wss

	private OkHttpManager okHttpManager = new OkHttpManager();
	private WebSocket webSocket = null;
	private LiveTexMessageProcessor messageProcessor = new LiveTexMessageProcessor();
	private LiveTexWebsocketListener websocketListener = new LiveTexWebsocketListener(messageProcessor);

	private NetworkManager() {
	}

	public static NetworkManager getInstance() {
		return instance;
	}

	public void connectWebSocket() throws IOException {
		if (webSocket != null) {
			Log.e(TAG, "connect: websocket is active!");
			return;
		}

		String clientId = "41b61e6a-bd25-4c5e-abe4-edeab10913d4"; // todo: load from sharedpref
		String url = HOST_WS.replace("{clientId}", clientId);

		Request request = new Request.Builder()
				.url(url)
				.build();
		webSocket = okHttpManager.webSocketConnection(request, websocketListener);
	}

	public void auth() throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(HOST_API + "auth").newBuilder();
		urlBuilder.addQueryParameter("touchPoint", "token");
		// todo: fill with all params
		String url = urlBuilder.build().toString();

		Request.Builder rb = new Request.Builder()
				.url(url)
				.get();

		String clientId = okHttpManager.requestString(rb.build());
		// todo: save to shared prefs
	}

	public void disconnectWebSocket() {
		if (webSocket != null) {
			webSocket.close(1000, "disconnect requested");
			webSocket = null;
		}
	}
}
