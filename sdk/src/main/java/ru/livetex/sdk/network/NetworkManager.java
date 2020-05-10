package ru.livetex.sdk.network;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.WebSocket;
import ru.livetex.sdk.LiveTex;
import ru.livetex.sdk.network.websocket.LiveTexWebsocketListener;

public final class NetworkManager {
	private static final String TAG = "NetworkManager";
	private static NetworkManager instance;

	private String HOST;

	private final OkHttpManager okHttpManager = new OkHttpManager();
	private final LiveTexWebsocketListener websocketListener;
	private final CompositeDisposable disposables = new CompositeDisposable();

	public enum ConnectionState {
		NOT_STARTED, // initial state
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}

	@Nullable
	private WebSocket webSocket = null;
	private boolean needReconnect = true;
	private BehaviorSubject<ConnectionState> connectionStateSubject = BehaviorSubject.createDefault(ConnectionState.NOT_STARTED);
	@NonNull
	private final String touchpoint;
	@Nullable
	private String deviceId;
	@Nullable
	private String deviceType;
	@Nullable
	private String lastClientId = null;

	private NetworkManager(@NonNull String host,
						   @NonNull String touchpoint,
						   @Nullable String deviceId,
						   @Nullable String deviceType) {
		this.HOST = host;
		this.touchpoint = touchpoint;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.websocketListener = LiveTex.getInstance().getWebsocketListener();
		subscribeToWebsocket();
	}

	public static void init(@NonNull String host,
							@NonNull String touchpoint,
							String deviceId,
							String deviceType) {
		instance = new NetworkManager(host, touchpoint, deviceId, deviceType);
	}

	public static NetworkManager getInstance() {
		return instance;
	}

	public Observable<ConnectionState> connectionState() {
		return connectionStateSubject;
	}

	public Single<String> connect(@Nullable String clientId) {
		return Single.fromCallable(() -> {
			lastClientId = auth(touchpoint, clientId, deviceId, deviceType);
			connectWebSocket();
			return lastClientId;
		});
	}

	private void connectWebSocket() throws IOException {
		if (webSocket != null) {
			Log.e(TAG, "Connect: websocket is active!");
			return;
		}
		if (lastClientId == null) {
			Log.e(TAG, "Connect: client id is null");
			return;
		}
		connectionStateSubject.onNext(ConnectionState.CONNECTING);

		String url = getWebsocketEndpoint().replace("{clientId}", lastClientId);

		Request request = new Request.Builder()
				.url(url)
				.build();
		webSocket = okHttpManager.webSocketConnection(request, websocketListener);
	}

	private String auth(@NonNull String touchpoint,
						@Nullable String clientId,
						@Nullable String deviceId,
						@Nullable String deviceType) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(getApiHost() + "auth")
				.newBuilder()
				.addQueryParameter("touchPoint", touchpoint);

		if (!TextUtils.isEmpty(clientId)) {
			urlBuilder.addQueryParameter("clientId", clientId);
		}
		if (!TextUtils.isEmpty(deviceId)) {
			urlBuilder.addQueryParameter("deviceId", deviceId);
		}
		if (!TextUtils.isEmpty(deviceType)) {
			urlBuilder.addQueryParameter("deviceType", deviceType);
		}
		String url = urlBuilder.build().toString();

		Request.Builder rb = new Request.Builder()
				.url(url)
				.get();

		// in future here we can receive new host for weboscket connection, so HOST variable can be updated
		return okHttpManager.requestString(rb.build());
	}

	public void forceDisconnect() {
		needReconnect = false;
		if (webSocket != null) {
			Log.i(TAG, "Disconnecting websocket...");
			webSocket.close(1000, "disconnect requested");
			connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
		} else {
			Log.i(TAG, "Websocket disconnect requested but websocket is null");
		}
	}

	@Nullable
	public WebSocket getWebSocket() {
		return webSocket;
	}

	private void subscribeToWebsocket() {
		disposables.add(websocketListener.disconnectEvent()
				.observeOn(Schedulers.io())
				.subscribe(ws -> {
					if (ws == webSocket) {
						webSocket = null;
						connectionStateSubject.onNext(ConnectionState.DISCONNECTED);

						if (needReconnect) {
							connectWebSocket();
						}
					}
				}, thr -> Log.e(TAG, "disconnectEvent", thr)));

		disposables.add(websocketListener.openEvent()
				.observeOn(Schedulers.io())
				.subscribe(ws -> {
					if (ws == webSocket) {
						connectionStateSubject.onNext(ConnectionState.CONNECTED);
					}
				}, thr -> Log.e(TAG, "openEvent", thr)));

		disposables.add(websocketListener.failEvent()
				.observeOn(Schedulers.io())
				.subscribe(pair -> {
					WebSocket ws = pair.first;
					Throwable thr = pair.second;
					if (ws == webSocket) {
						webSocket = null;
						connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
						needReconnect = thr instanceof SocketTimeoutException;

						// can be endless loop, so handle only SocketTimeoutException
						if (needReconnect) {
							disposables.add(Single.timer(3, TimeUnit.SECONDS)
									.observeOn(Schedulers.io())
									.subscribe(ignore -> {
										connectWebSocket();
									}, thr1 -> Log.e(TAG, "reconnect", thr1)));
						}
					}
				}, thr -> Log.e(TAG, "failEvent", thr)));
	}

	private String getApiHost() {
		return "http://" + HOST + "v1/"; // todo: https
	}

	private String getWebsocketEndpoint() {
		return "ws://" + HOST + "v1/ws/{clientId}"; // todo: wss
	}
}
