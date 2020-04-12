package ru.livetex.sdk;

public final class LiveTex {

	private static LiveTex instance = null;

	private LiveTex() {
	}

	private LiveTex(Builder builder) {
	}

	public static LiveTex getInstance() {
		if (instance == null) {
			// todo: write about builder
			throw new IllegalStateException("LiveText getInstance() method called too early.");
		}
		return instance;
	}

	// todo: setup here variables and controllers classes
	public static class Builder {
		public Builder() {

		}

		public void build() {
			instance = new LiveTex(this);
		}
	}
}
