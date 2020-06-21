package ru.livetex.demoapp.db.entity;

import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.livetex.sdk.entity.Creator;
import ru.livetex.sdk.entity.User;

public final class ChatMessage implements Comparable<ChatMessage> {
	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public Date createdAt; // timestamp in millis
	@NonNull
	public final Creator creator;

	@Nullable
	public final String fileUrl; // in case of "file" message
	public final boolean isIncoming;
	public MessageSentState sentState;

	// new local text\file message
	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt) {
		this.id = id;
		this.content = content;
		this.createdAt = createdAt;
		this.isIncoming = false;
		this.sentState = MessageSentState.NOT_SENT;
		this.fileUrl = null;
		this.creator = new User();
	}

	// mapped from server text entity
	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt, boolean isIncoming, Creator creator) {
		this(id, content, createdAt, isIncoming, null, creator);
	}

	// mapped from server file entity
	public ChatMessage(@NonNull String id, @NonNull String content, @NonNull Date createdAt, boolean isIncoming, @Nullable String fileUrl, @NonNull Creator creator) {
		this.id = id;
		this.content = content;
		this.createdAt = createdAt;
		this.isIncoming = isIncoming;
		this.sentState = MessageSentState.SENT;
		this.fileUrl = fileUrl;
		this.creator = creator;
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
				sentState == that.sentState &&
				creator == that.creator;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, content, createdAt, isIncoming, sentState, creator);
	}

	@Override
	public int compareTo(ChatMessage o) {
		return this.createdAt.compareTo(o.createdAt);
	}
}
