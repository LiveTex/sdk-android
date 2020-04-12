package ru.livetex.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.livetex.sdk.logic.LiveTexMessageHandler;
import ru.livetex.sdk.network.NetworkManager;
import ru.livetex.sdk.network.websocket.LiveTexWebsocketListener;

public final class LiveTex {

	private static LiveTex instance = null;

	private final LiveTexMessageHandler messageHandler;

	private LiveTex(Builder builder) {
		this.messageHandler = builder.messageHandler;
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

	public LiveTexMessageHandler getMessageHandler() {
		return messageHandler;
	}

	public static class Builder {
		@NonNull
		private final String touchpoint;
		@Nullable
		private String deviceId = null;
		@Nullable
		private String deviceType = null;
		// todo: setters with desc
		private LiveTexMessageHandler messageHandler = new LiveTexMessageHandler();
		private LiveTexWebsocketListener websocketListener = new LiveTexWebsocketListener(messageHandler);

		public Builder(@NonNull String touchpoint) {
			this.touchpoint = touchpoint;
		}

		public void build() {
			NetworkManager.init(touchpoint, deviceId, deviceType, websocketListener);
			instance = new LiveTex(this);
		}
	}
}
