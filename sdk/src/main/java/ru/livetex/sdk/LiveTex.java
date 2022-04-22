package ru.livetex.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.livetex.sdk.logic.LiveTexMessagesHandler;
import ru.livetex.sdk.network.NetworkManager;
import ru.livetex.sdk.network.websocket.LiveTexWebsocketListener;

public final class LiveTex {

	private static LiveTex instance = null;

	private final LiveTexWebsocketListener websocketListener;
	private final LiveTexMessagesHandler messagesHandler;

	private LiveTex(Builder builder) {
		this.messagesHandler = builder.messageHandler;
		this.websocketListener = builder.websocketListener;
	}

	public static LiveTex getInstance() {
		if (instance == null) {
			throw new IllegalStateException("LiveText getInstance() method called too early. Create LiveText instance with LiveTex.Builder()");
		}
		return instance;
	}

	public NetworkManager getNetworkManager() {
		return NetworkManager.getInstance();
	}

	public LiveTexMessagesHandler getMessagesHandler() {
		return messagesHandler;
	}

	public LiveTexWebsocketListener getWebsocketListener() {
		return websocketListener;
	}

	public static class Builder {
		@NonNull
		private String host = "visitor-api.livetex.ru/";
		@NonNull
		private String authEndpoint = "https://visitor-api.livetex.ru/v1/auth";
		@NonNull
		private final String touchpoint;
		@Nullable
		private String deviceToken = null;
		@NonNull
		private final String deviceType = "android";

		private boolean isNetworkLoggingEnabled = false;
		private boolean isWebsocketLoggingEnabled = false;

		private LiveTexMessagesHandler messageHandler = new LiveTexMessagesHandler();
		private LiveTexWebsocketListener websocketListener = new LiveTexWebsocketListener();

		public Builder(@NonNull String touchpoint) {
			this.touchpoint = touchpoint;
		}

		/**
		 * Set custom host in format "[subdomain.]domain.zone/"
		 * Host used for for websocket connection (mostly can be changed by auth response) and files upload
		 */
		public Builder setHost(@NonNull String host) {
			this.host = host;
			return this;
		}

		/**
		 * Set custom auth endpoint like "https://visitor-api.livetex.ru/v1/auth"
		 * Used for auth request
		 */
		public Builder setAuthEndpoint(@NonNull String authEndpoint) {
			this.authEndpoint = authEndpoint;
			return this;
		}

		/**
		 * deviceToken is unique device identifier. Used for pushes, so it should be Firebase token.
		 */
		public Builder setDeviceToken(String deviceToken) {
			this.deviceToken = deviceToken;
			return this;
		}

		/**
		 * Set custom messages handler
		 */
		public Builder setMessageHandler(LiveTexMessagesHandler messageHandler) {
			this.messageHandler = messageHandler;
			return this;
		}

		/**
		 * Set custom websocket listener
		 */
		public Builder setWebsocketListener(LiveTexWebsocketListener websocketListener) {
			this.websocketListener = websocketListener;
			return this;
		}

		/**
		 * Enable logging of (non-websocket) communication (for debug)
		 */
		public Builder setNetworkLoggingEnabled() {
			this.isNetworkLoggingEnabled = true;
			return this;
		}

		/**
		 * Enable logging of websocket communication (for debug)
		 */
		public Builder setWebsocketLoggingEnabled() {
			this.isWebsocketLoggingEnabled = true;
			return this;
		}

		public void build() {
			instance = new LiveTex(this);
			NetworkManager.init(host, authEndpoint, touchpoint, deviceToken, deviceType, isNetworkLoggingEnabled);
			messageHandler.init(isWebsocketLoggingEnabled);
			websocketListener.init();
		}
	}
}