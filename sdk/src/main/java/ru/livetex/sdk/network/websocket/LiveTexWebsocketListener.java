package ru.livetex.sdk.network.websocket;

import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import ru.livetex.sdk.logic.LiveTexMessagesHandler;

// todo: logging flag
public class LiveTexWebsocketListener extends WebSocketListener {
	private static final String TAG = "LTWebsocketListener";

	private final LiveTexMessagesHandler messageHandler;
	private final PublishSubject<WebSocket> disconnectEvent = PublishSubject.create();
	private final PublishSubject<WebSocket> openEvent = PublishSubject.create();
	private final PublishSubject<WebSocket> failEvent = PublishSubject.create();

	public LiveTexWebsocketListener(LiveTexMessagesHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		Log.i(TAG, "opened");
		openEvent.onNext(webSocket);
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

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response) {
		Log.e(TAG, "failed with reason " + t.getMessage(), t);
		failEvent.onNext(webSocket);
	}

	// Should be used only by NetworkManager because in case of force disconnect it doesn't trigger!
	public PublishSubject<WebSocket> disconnectEvent() {
		return disconnectEvent;
	}

	public PublishSubject<WebSocket> openEvent() {
		return openEvent;
	}

	public PublishSubject<WebSocket> failEvent() {
		return failEvent;
	}

	public LiveTexMessagesHandler getMessagesHandler() {
		return messageHandler;
	}
}
