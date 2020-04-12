package ru.livetex.sdk.network;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

// todo: interface or base class
public class LiveTexWebsocketListener extends WebSocketListener {

	private static final String TAG = "LiveTexWebsocketListener";
	private final LiveTexMessageProcessor messageProcessor;

	public LiveTexWebsocketListener(LiveTexMessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}
	// todo: logging flag

	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		Log.i(TAG, "opened");
	}

	@Override
	public void onMessage(WebSocket webSocket, String text) {
		messageProcessor.onMessage(text);
	}

	@Override
	public void onMessage(WebSocket webSocket, ByteString bytes) {
		messageProcessor.onDataMessage(bytes);
	}

	@Override
	public void onClosing(WebSocket webSocket, int code, String reason) {
		Log.i(TAG, "closing with reason " + reason);
	}

	@Override
	public void onClosed(WebSocket webSocket, int code, String reason) {
		Log.i(TAG, "closed with reason " + reason);
	}
}
