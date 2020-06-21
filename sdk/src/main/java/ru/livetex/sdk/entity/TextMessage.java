package ru.livetex.sdk.entity;

import java.util.Date;

import androidx.annotation.NonNull;

public final class TextMessage extends BaseEntity implements GenericMessage {
	public static final String TYPE = "text";

	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public Date createdAt;
	@NonNull
	public transient Creator creator;

	public TextMessage(@NonNull String text) {
		super();
		this.content = text;
	}

	@Override
	@NonNull
	public Creator getCreator() {
		return creator;
	}

	@Override
	public void setCreator(@NonNull Creator creator) {
		this.creator = creator;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
