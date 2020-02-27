package ru.livetex.sdk.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.WebSocket;

public final class NetworkManager {
	private static final String TAG = "NetworkManager";
	private static NetworkManager instance = new NetworkManager();

	private OkHttpManager okHttpManager = new OkHttpManager();
	private WebSocket webSocket = null;
	private LiveTexMessageProcessor messageProcessor = new LiveTexMessageProcessor();
	private LiveTexWebsocketListener websocketListener = new LiveTexWebsocketListener(messageProcessor);

	private NetworkManager() {
	}

	public static NetworkManager getInstance() {
		return instance;
	}

	public void connect() throws IOException {
		if (webSocket != null) {
			Log.e(TAG, "connect: websocket is active!");
			return;
		}
		Request request = new Request.Builder()
				.url("wss://echo.websocket.org") // todo:
				.build();
		webSocket = okHttpManager.webSocketConnection(request, websocketListener);
	}

	public void disconnect() {
		if (webSocket != null) {
			webSocket.close(1000, "disconnect requested");
			webSocket = null;
		}
	}
}
