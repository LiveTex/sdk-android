package ru.livetex.sdk.network.websocket;

import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import ru.livetex.sdk.BuildConfig;
import ru.livetex.sdk.LiveTex;
import ru.livetex.sdk.logic.LiveTexMessagesHandler;

public class LiveTexWebsocketListener extends WebSocketListener {
	protected static final String TAG = "LTWebsocketListener";
	protected static final Boolean LOGGING = BuildConfig.DEBUG;

	protected LiveTexMessagesHandler messageHandler;

	private final PublishSubject<WebSocket> disconnectEvent = PublishSubject.create();
	private final PublishSubject<WebSocket> openEvent = PublishSubject.create();
	private final PublishSubject<WebSocket> failEvent = PublishSubject.create();

	public void init() {
		this.messageHandler = LiveTex.getInstance().getMessagesHandler();
	}

	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		if (LOGGING) {
			Log.i(TAG, "Opened");
		}
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
		if (LOGGING) {
			Log.i(TAG, "Closing with reason " + reason);
		}
	}

	@Override
	public void onClosed(WebSocket webSocket, int code, String reason) {
		if (LOGGING) {
			Log.i(TAG, "Closed with reason " + reason);
		}
		disconnectEvent.onNext(webSocket);
	}

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response) {
		if (LOGGING) {
			Log.e(TAG, "Failed with reason " + t.getMessage(), t);
		}
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
}
