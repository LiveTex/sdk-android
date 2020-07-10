package ru.livetex.sdk.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

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
import ru.livetex.sdk.entity.AuthResponseEntity;
import ru.livetex.sdk.network.websocket.LiveTexWebsocketListener;

public final class NetworkManager {
	private static final String TAG = "NetworkManager";
	private static NetworkManager instance;

	private final OkHttpManager okHttpManager = new OkHttpManager();
	private final ApiManager apiManager = new ApiManager(okHttpManager);
	private final LiveTexWebsocketListener websocketListener;
	private final CompositeDisposable disposables = new CompositeDisposable();

	public enum ConnectionState {
		NOT_STARTED, // initial state
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}

	// Endpoint for auth request.
	private String authHost;
	// Endpoint for web socket connection. Can be changed by auth response.
	private String wsEndpoint;
	// Endpoint for file upload. Can be changed by auth response.
	private String uploadEndpoint;
	@NonNull
	private final String touchpoint;
	@Nullable
	private final String deviceToken;
	@Nullable
	private final String deviceType;
	@Nullable
	private String lastUserToken = null;
	@Nullable
	private WebSocket webSocket = null;
	private boolean needReconnect = true;
	private final BehaviorSubject<ConnectionState> connectionStateSubject = BehaviorSubject.createDefault(ConnectionState.NOT_STARTED);
	private final NetworkStateObserver networkStateObserver = new NetworkStateObserver();

	private NetworkManager(@NonNull String host,
						   @NonNull String touchpoint,
						   @Nullable String deviceToken,
						   @Nullable String deviceType) {
		this.authHost = "https://" + host + "v1/";
		this.wsEndpoint = "wss://" + host + "v1/ws/{userToken}";
		this.uploadEndpoint = "https://" + host + "v1/upload";
		this.touchpoint = touchpoint;
		this.deviceToken = deviceToken;
		this.deviceType = deviceType;
		this.websocketListener = LiveTex.getInstance().getWebsocketListener();
		subscribeToWebsocket();

		disposables.add(networkStateObserver.status()
				.filter(status -> status == NetworkStateObserver.InternetConnectionStatus.CONNECTED)
				.observeOn(Schedulers.io())
				.subscribe(status -> {
					if (needReconnect && connectionStateSubject.getValue() == ConnectionState.DISCONNECTED) {
						connectWebSocket();
					}
				}, thr -> {
					Log.e(TAG, "networkStateObserver", thr);
				}));
	}

	public static void init(@NonNull String host,
							@NonNull String touchpoint,
							String deviceToken,
							String deviceType) {
		instance = new NetworkManager(host, touchpoint, deviceToken, deviceType);
	}

	public static NetworkManager getInstance() {
		return instance;
	}

	public ApiManager getApiManager() {
		return apiManager;
	}

	public Observable<ConnectionState> connectionState() {
		return connectionStateSubject;
	}

	public void startObserveNetworkState(Context context) {
		networkStateObserver.startObserve(context);
	}

	public void stopObserveNetworkState(Context context) {
		networkStateObserver.stopObserve(context);
	}

	/**
	 * Do authorization and connect to chat websocket.
	 * @param userToken - token (or id) which identifies a current user. Can be null if user is new. For AuthTokenType.CUSTOM it should be user id in your system.
	 * @param authTokenType - AuthTokenType.DEFAULT for standard (LiveTex) token system.
	 */
	public Single<String> connect(@Nullable String userToken, AuthTokenType authTokenType) {
		return Single.fromCallable(() -> {
			switch (authTokenType) {
				case DEFAULT:
					lastUserToken = auth(touchpoint, userToken, deviceToken, deviceType, null);
					break;
				case CUSTOM:
					lastUserToken = auth(touchpoint, null, deviceToken, deviceType, userToken);
					break;
			}

			connectWebSocket();
			return lastUserToken;
		});
	}

	@Nullable
	public WebSocket getWebSocket() {
		return webSocket;
	}

	public String getUploadEndpoint() {
		return uploadEndpoint;
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

	private void connectWebSocket() {
		if (webSocket != null) {
			Log.e(TAG, "Connect: websocket is active!");
			return;
		}
		if (lastUserToken == null) {
			Log.e(TAG, "Connect: client token is null");
			return;
		}
		connectionStateSubject.onNext(ConnectionState.CONNECTING);

		String url = wsEndpoint.replace("{userToken}", lastUserToken);

		Request request = new Request.Builder()
				.url(url)
				.build();
		webSocket = okHttpManager.webSocketConnection(request, websocketListener);
	}

	private String auth(@NonNull String touchpoint,
						@Nullable String userToken,
						@Nullable String deviceToken,
						@Nullable String deviceType,
						@Nullable String customUserToken) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(authHost + "auth")
				.newBuilder()
				.addQueryParameter("touchPoint", touchpoint);

		if (!TextUtils.isEmpty(userToken)) {
			urlBuilder.addQueryParameter("userToken", userToken);
		}
		if (!TextUtils.isEmpty(customUserToken)) {
			urlBuilder.addQueryParameter("customUserToken", customUserToken);
		}
		if (!TextUtils.isEmpty(deviceToken)) {
			urlBuilder.addQueryParameter("deviceToken", deviceToken);
		}
		if (!TextUtils.isEmpty(deviceType)) {
			urlBuilder.addQueryParameter("deviceType", deviceType);
		}
		String url = urlBuilder.build().toString();

		Request.Builder rb = new Request.Builder()
				.url(url)
				.get();

		String response = okHttpManager.requestString(rb.build());
		AuthResponseEntity responseEntity = new Gson().fromJson(response, AuthResponseEntity.class);
		if (!TextUtils.isEmpty(responseEntity.endpoints.ws)) {
			wsEndpoint = responseEntity.endpoints.ws;
		}
		if (!TextUtils.isEmpty(responseEntity.endpoints.upload)) {
			uploadEndpoint = responseEntity.endpoints.upload;
		}
		return responseEntity.userToken;
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
						needReconnect = true;
						boolean needReconnectNow = networkStateObserver.getStatus() == NetworkStateObserver.InternetConnectionStatus.CONNECTED;

						// Can be endless loop, so handle only SocketTimeoutException
						if (needReconnectNow) {
							disposables.add(Single.timer(3, TimeUnit.SECONDS)
									.observeOn(Schedulers.io())
									.subscribe(ignore -> {
										connectWebSocket();
									}, thr1 -> Log.e(TAG, "reconnect", thr1)));
						} else {
							// Reconnect should be done later manually or by network state observer
						}
					}
				}, thr -> Log.e(TAG, "failEvent", thr)));
	}
}
