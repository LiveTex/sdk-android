package ru.livetex.demoapp.db.entity;

import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ChatMessage {
	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public Date createdAt; // timestamp in millis

	@Nullable
	public String fileUrl; // in case of "file" message
	public boolean isIncoming;
	public MessageSentState sentState;

	// new local text\file message
	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt) {
		this.id = id;
		this.content = content;
		this.createdAt = createdAt;
		this.isIncoming = false;
		this.sentState = MessageSentState.NOT_SENT;
		this.fileUrl = null;
	}

	// mapped from server text entity
	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt, boolean isIncoming) {
		this(id, content, createdAt, isIncoming, null);
	}

	// mapped from server file entity
	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt, boolean isIncoming, @Nullable String fileUrl) {
		this.id = id;
		this.content = content;
		this.createdAt = createdAt;
		this.isIncoming = isIncoming;
		this.sentState = MessageSentState.SENT;
		this.fileUrl = fileUrl;
	}

	public void setSentState(MessageSentState sentState) {
		this.sentState = sentState;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ChatMessage)) {
			return false;
		}
		ChatMessage that = (ChatMessage) o;
		return isIncoming == that.isIncoming &&
				id.equals(that.id) &&
				content.equals(that.content) &&
				createdAt.equals(that.createdAt) &&
				sentState == that.sentState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, content, createdAt, isIncoming, sentState);
	}
}
