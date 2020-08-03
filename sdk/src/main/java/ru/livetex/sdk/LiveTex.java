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
		private String host = "visitor-auth.livetex.ru/";
		@NonNull
		private final String touchpoint;
		@Nullable
		private String deviceToken = null;
		@NonNull
		private final String deviceType = "android";

		private LiveTexMessagesHandler messageHandler = new LiveTexMessagesHandler();
		private LiveTexWebsocketListener websocketListener = new LiveTexWebsocketListener();

		public Builder(@NonNull String touchpoint) {
			this.touchpoint = touchpoint;
		}

		/**
		 * Set custom host in format "[subdomain.]domain.zone/"
		 * Host used for auth request and for websocket connection (but can be changed by auth response)
		 */
		public Builder setHost(@NonNull String host) {
			this.host = host;
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

		public void build() {
			instance = new LiveTex(this);
			NetworkManager.init(host, touchpoint, deviceToken, deviceType);
			messageHandler.init();
			websocketListener.init();
		}
	}
}