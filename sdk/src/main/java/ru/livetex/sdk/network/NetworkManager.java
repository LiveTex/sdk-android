package ru.livetex.sdk.network;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.WebSocket;
import ru.livetex.sdk.network.websocket.LiveTexWebsocketListener;

public final class NetworkManager {
	private static final String TAG = "NetworkManager";
	private static NetworkManager instance;

	private String HOST;

	private final OkHttpManager okHttpManager = new OkHttpManager();
	private final LiveTexWebsocketListener websocketListener;
	private final CompositeDisposable disposables = new CompositeDisposable();

	public enum ConnectionState {
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}

	@Nullable
	private WebSocket webSocket = null;
	private BehaviorSubject<ConnectionState> connectionStateSubject = BehaviorSubject.createDefault(ConnectionState.DISCONNECTED);
	@NonNull
	private final String touchpoint;
	@Nullable
	private String deviceId;
	@Nullable
	private String deviceType;

	private NetworkManager(@NonNull String host,
						   @NonNull String touchpoint,
						   @Nullable String deviceId,
						   @Nullable String deviceType,
						   LiveTexWebsocketListener websocketListener) {
		this.HOST = host;
		this.touchpoint = touchpoint;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.websocketListener = websocketListener;
	}

	public static void init(@NonNull String host,
							@NonNull String touchpoint,
							String deviceId,
							String deviceType,
							LiveTexWebsocketListener websocketListener) {
		instance = new NetworkManager(host, touchpoint, deviceId, deviceType, websocketListener);
	}

	public static NetworkManager getInstance() {
		return instance;
	}

	public Observable<ConnectionState> connectionState() {
		return connectionStateSubject;
	}

	// todo: rx
	@WorkerThread
	public void connect(@Nullable String clientId,
						@NonNull AuthConnectionListener listener) {
		try {
			clientId = auth(touchpoint, clientId, deviceId, deviceType);
			connectWebSocket(clientId);
			listener.onAuthSuccess(clientId);
		} catch (IOException e) {
			listener.onAuthError(e);
		}
	}

	private void connectWebSocket(@NonNull String clientId) throws IOException {
		if (webSocket != null) {
			Log.e(TAG, "connect: websocket is active!");
			return;
		}
		connectionStateSubject.onNext(ConnectionState.CONNECTING);

		String url = getWebsocketEndpoint().replace("{clientId}", clientId);

		Request request = new Request.Builder()
				.url(url)
				.build();
		webSocket = okHttpManager.webSocketConnection(request, websocketListener);

		connectionStateSubject.onNext(ConnectionState.CONNECTED);

		disposables.add(websocketListener.disconnectEvent()
				.observeOn(Schedulers.io())
				.subscribe(ws -> {
					if (ws == webSocket) {
						webSocket = null;
						disposables.clear();
						connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
						// todo: reconnect if need
					}
				}, thr -> Log.e(TAG, "", thr)));
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

		// todo: error handling
		// todo: in future here we can receive new host for weboscket connection, so HOST variable can be updated
		return okHttpManager.requestString(rb.build());
	}

	public void disconnect() {
		if (webSocket != null) {
			webSocket.close(1000, "disconnect requested");
			connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
			webSocket = null;
		}
		disposables.clear();
	}

	@Nullable
	public WebSocket getWebSocket() {
		return webSocket;
	}

	private String getApiHost() {
		return "http://" + HOST + "v1/"; // todo: https
	}

	private String getWebsocketEndpoint() {
		return "ws://" + HOST + "v1/ws/{clientId}"; // todo: wss
	}
}
