package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public final class TextMessage extends BaseEntity implements GenericMessage {
	public static final String TYPE = "text";

	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public String createdAt;
	@NonNull
	public Object creator; // todo: ! Employee or User, will be changed here

	public TextMessage(String text) {
		super();
		this.content = text;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
