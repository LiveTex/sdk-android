package ru.livetex.sdk.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.Functions;
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

	private final OkHttpManager okHttpManager;
	private final ApiManager apiManager;
	private final LiveTexWebsocketListener websocketListener;
	private final CompositeDisposable disposables = new CompositeDisposable();
	private final BehaviorSubject<ConnectionState> connectionStateSubject = BehaviorSubject.createDefault(ConnectionState.NOT_STARTED);
	private final BehaviorSubject<Boolean> connectionFailSubject = BehaviorSubject.createDefault(false);
	private final NetworkStateObserver networkStateObserver = new NetworkStateObserver();

	public enum ConnectionState {
		NOT_STARTED, // initial state
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}

	// Endpoint for auth request.
	private final String authEndpoint;
	// Endpoint for web socket connection. Will be changed by auth response.
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
	private String lastVisitorToken = null;
	@Nullable
	private AuthData authData = null;
	@Nullable
	private WebSocket webSocket = null;
	private boolean reconnectRequired = true;

	private NetworkManager(@NonNull String host,
						   @NonNull String authEndpoint,
						   @NonNull String touchpoint,
						   @Nullable String deviceToken,
						   @Nullable String deviceType,
						   boolean isNetworkLoggingEnabled) {
		this.authEndpoint = authEndpoint;
		this.wsEndpoint = "wss://" + host + "v1/ws/{visitorToken}"; // just a placeholder, will be replaced by auth response
		this.uploadEndpoint = "https://" + host + "v1/upload";
		this.touchpoint = touchpoint;
		this.deviceToken = deviceToken;
		this.deviceType = deviceType;
		this.okHttpManager = new OkHttpManager(isNetworkLoggingEnabled);
		this.apiManager = new ApiManager(okHttpManager);
		this.websocketListener = LiveTex.getInstance().getWebsocketListener();

		subscribeToWebsocket();

		disposables.add(Observable.combineLatest(networkStateObserver.status(), connectionFailSubject, Pair::new)
				.observeOn(Schedulers.io())
				.map(pair -> pair.first)
				.flatMapCompletable(status -> {
					if (status == NetworkStateObserver.InternetConnectionStatus.CONNECTED) {
						if (reconnectRequired &&
								connectionStateSubject.getValue() == ConnectionState.DISCONNECTED &&
								authData != null) {
							return connect(authData, true)
									.ignoreElement()
									.retry(thr -> thr instanceof SocketTimeoutException)
									.onErrorComplete(thr -> {
										Log.e(TAG, "networkStateObserver", thr);
										return true;
									});
						} else {
							return Completable.complete();
						}
					} else {
						// Websocket reaction can be delayed but it's a traditional way to properly finish connection. So don't force to close.
//						if (connectionStateSubject.getValue() != ConnectionState.DISCONNECTED) {
//							connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
//						}
						return Completable.complete();
					}
				})
				.subscribe(Functions.EMPTY_ACTION, thr -> Log.e(TAG, "networkStateObserver", thr)));
	}

	public static void init(@NonNull String host,
							@NonNull String authEndpoint,
							@NonNull String touchpoint,
							String deviceToken,
							String deviceType,
							boolean isNetworkLoggingEnabled) {
		instance = new NetworkManager(host, authEndpoint, touchpoint, deviceToken, deviceType, isNetworkLoggingEnabled);
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
	 * @param authData user data for authorization.
	 * @param reconnectRequired set true (recommended) to automatically reconnect (auth + websocket).
	 * @return newly generated visitorToken if token in AuthData was null, or same token which was provided
	 */
	public Single<String> connect(@NonNull AuthData authData, boolean reconnectRequired) {
		return Single.create(emitter -> {
			this.authData = authData;
			this.reconnectRequired = reconnectRequired;

			if (connectionStateSubject.getValue() == ConnectionState.CONNECTING || connectionStateSubject.getValue() == ConnectionState.CONNECTED) {
				emitter.tryOnError(new IllegalStateException("Trying to connect with incorrect state! " + connectionStateSubject.getValue()));
				return;
			}

			connectionStateSubject.onNext(ConnectionState.CONNECTING);
			try {
				lastVisitorToken = auth(touchpoint, authData.visitorToken, deviceToken, deviceType, authData.customVisitorToken);
			} catch (Exception e) {
				connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
				emitter.tryOnError(e);
				return;
			}

			onVisitorTokenUpdated();
			connectWebSocket();
			emitter.onSuccess(lastVisitorToken);
		});
	}

	/**
	 * Do authorization and connect to chat websocket.
	 * @param visitorToken token (or id) which identifies a current user. Can be null if user is new. For AuthTokenType.CUSTOM it should be user id in your system.
	 * @param authTokenType AuthTokenType.DEFAULT for standard (LiveTex) token system.
	 * @return newly generated visitorToken if param was null, or same visitorToken which was provided
	 */
	@Deprecated // Use connect with AuthData instead
	public Single<String> connect(@Nullable String visitorToken,
								  AuthTokenType authTokenType) {
		return connect(visitorToken, authTokenType, true);
	}

	/**
	 * Do authorization and connect to chat websocket.
	 * @param visitorToken - token (or id) which identifies a current user. Can be null if AuthTokenType.DEFAULT and user is new. For AuthTokenType.CUSTOM it should be user id in your system.
	 * @param authTokenType - AuthTokenType.DEFAULT for standard (LiveTex) token system.
	 * @param reconnectRequired - set true to automatically reconnect (auth + websocket).
	 * @return newly generated visitorToken if param was null, or same visitorToken which was provided
	 */
	@Deprecated // Use connect with AuthData instead
	public Single<String> connect(@Nullable String visitorToken,
								  AuthTokenType authTokenType,
								  boolean reconnectRequired) {
		return Single.create(emitter -> {
			this.reconnectRequired = reconnectRequired;

			if (connectionStateSubject.getValue() == ConnectionState.CONNECTING || connectionStateSubject.getValue() == ConnectionState.CONNECTED) {
				emitter.tryOnError(new IllegalStateException("Trying to connect with incorrect state! " + connectionStateSubject.getValue()));
				return;
			}

			switch (authTokenType) {
				case DEFAULT:
					this.authData = AuthData.withVisitorToken(visitorToken);
					break;
				case CUSTOM:
					if (visitorToken == null) {
						emitter.tryOnError(new IllegalArgumentException("For AuthTokenType.CUSTOM visitorToken can't be null"));
						return;
					}
					this.authData = AuthData.withCustomVisitorToken(visitorToken);
					break;
			}

			connectionStateSubject.onNext(ConnectionState.CONNECTING);

			try {
				lastVisitorToken = auth(touchpoint, authData.visitorToken, deviceToken, deviceType, authData.customVisitorToken);
			} catch (Exception e) {
				connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
				emitter.tryOnError(e);
				return;
			}

			onVisitorTokenUpdated();
			connectWebSocket();
			emitter.onSuccess(lastVisitorToken);
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
		reconnectRequired = false;
		authData = null;

		if (webSocket != null) {
			Log.i(TAG, "Disconnecting websocket...");
			webSocket.close(1000, "disconnect requested");
			if (connectionStateSubject.getValue() != ConnectionState.DISCONNECTED) {
				connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
			}
		} else {
			Log.i(TAG, "Websocket disconnect requested but websocket is null");
		}
	}

	private void connectWebSocket() {
		if (lastVisitorToken == null) {
			Log.e(TAG, "Connect: visitor token is null");
			connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
			return;
		}
		if (webSocket != null) {
			Log.e(TAG, "Connect: websocket is active!");
			return;
		}

		String url = wsEndpoint.replace("{visitorToken}", lastVisitorToken);

		Request request = new Request.Builder()
				.url(url)
				.build();
		webSocket = okHttpManager.webSocketConnection(request, websocketListener);
	}

	private String auth(@NonNull String touchpoint,
						@Nullable String visitorToken,
						@Nullable String deviceToken,
						@Nullable String deviceType,
						@Nullable String customVisitorToken) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(authEndpoint)
				.newBuilder()
				.addQueryParameter("touchPoint", touchpoint);

		if (!TextUtils.isEmpty(visitorToken)) {
			urlBuilder.addQueryParameter("visitorToken", visitorToken);
		}
		if (!TextUtils.isEmpty(customVisitorToken)) {
			urlBuilder.addQueryParameter("customVisitorToken", customVisitorToken);
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
		return responseEntity.visitorToken;
	}

	private void subscribeToWebsocket() {
		disposables.add(websocketListener.disconnectEvent()
				.observeOn(Schedulers.io())
				.subscribe(ws -> {
					if (ws == webSocket) {
						webSocket = null;
						if (connectionStateSubject.getValue() != ConnectionState.DISCONNECTED) {
							connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
						}

						if (reconnectRequired) {
							connectionFailSubject.onNext(true);
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
					Throwable reason = pair.second;

					if (ws == webSocket) {
						webSocket = null;
						if (connectionStateSubject.getValue() != ConnectionState.DISCONNECTED) {
							connectionStateSubject.onNext(ConnectionState.DISCONNECTED);
						}
						boolean needReconnect = reconnectRequired && networkStateObserver.getStatus() == NetworkStateObserver.InternetConnectionStatus.CONNECTED;

						// Can be endless loop, so handle only SocketTimeoutException
						if (needReconnect) {
							disposables.add(Single.timer(3, TimeUnit.SECONDS)
									.subscribe(ignore -> {
										connectionFailSubject.onNext(true);
									}, thr1 -> Log.e(TAG, "reconnect", thr1)));
						} else {
							// Reconnect should be done later manually or by network state observer
						}
					}
				}, thr -> Log.e(TAG, "failEvent", thr)));
	}

	private void onVisitorTokenUpdated() {
		apiManager.setAuthToken(lastVisitorToken);
	}
}
