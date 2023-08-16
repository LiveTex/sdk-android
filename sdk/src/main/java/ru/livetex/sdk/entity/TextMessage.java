package ru.livetex.sdk.entity;

import java.util.Date;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class TextMessage extends BaseEntity implements GenericMessage {
	public static final String TYPE = "text";

	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public Date createdAt;
	@NonNull
	public Creator creator;
	// Related to custom project logic
	@Nullable
	public Map<String, String> attributes = null;
	// Message can have action buttons
	@Nullable
	public KeyboardEntity keyboard = null;

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
