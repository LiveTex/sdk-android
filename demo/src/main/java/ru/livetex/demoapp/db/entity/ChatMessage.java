package ru.livetex.demoapp.db.entity;

import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;

public final class ChatMessage {
	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public Date createdAt; // timestamp in millis
	public boolean isIncoming;

	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt, boolean isIncoming) {
		this.id = id;
		this.content = content;
		this.createdAt = createdAt;
		this.isIncoming = isIncoming;
	}

	// todo: send state

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
				createdAt.equals(that.createdAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, content, createdAt, isIncoming);
	}
}
