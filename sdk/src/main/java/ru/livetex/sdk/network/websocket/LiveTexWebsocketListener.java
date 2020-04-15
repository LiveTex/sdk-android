package ru.livetex.sdk.network.websocket;

import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import ru.livetex.sdk.logic.LiveTexMessagesHandler;

// todo: interface or base class
// todo: logging flag
public class LiveTexWebsocketListener extends WebSocketListener {

	private static final String TAG = "LTWebsocketListener";
	private final LiveTexMessagesHandler messageHandler;
	private final PublishSubject<WebSocket> disconnectEvent = PublishSubject.create();

	public LiveTexWebsocketListener(LiveTexMessagesHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		Log.i(TAG, "opened");
	}

	@Override
	public void onMessage(WebSocket webSocket, String text) {
		messageHandler.onMessage(text);
	}

	@Override
	public void onMessage(WebSocket webSocket, ByteString bytes) {
		messageHandler.onDataMessage(bytes);
	}

	@Override
	public void onClosing(WebSocket webSocket, int code, String reason) {
		Log.i(TAG, "closing with reason " + reason);
	}

	@Override
	public void onClosed(WebSocket webSocket, int code, String reason) {
		Log.i(TAG, "closed with reason " + reason);
		disconnectEvent.onNext(webSocket);
	}

	public PublishSubject<WebSocket> disconnectEvent() {
		return disconnectEvent;
	}
}
