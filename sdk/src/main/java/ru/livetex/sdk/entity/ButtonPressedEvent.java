package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public final class ButtonPressedEvent extends BaseEntity {
	public static final String TYPE = "buttonPressed";

	public ButtonPressedEvent(@NonNull String payload) {
		this.payload = payload;
	}

	@NonNull
	public String payload;

	@Override
	protected String getType() {
		return TYPE;
	}
}
