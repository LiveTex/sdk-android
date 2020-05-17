package ru.livetex.demoapp.ui.adapter;

import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.livetex.demoapp.db.entity.ChatMessage;
import ru.livetex.demoapp.db.entity.MessageSentState;

/**
 * This is wrapper for ChatMessage entity. It allows to use only UI data and also made adapter item mutable (for DiffUtil).
 * In real project fields will differ.
 */
public class ChatItem implements Comparable<ChatItem> {
	@NonNull
	public String id;
	@NonNull
	public String content;
	@NonNull
	public Date createdAt; // timestamp in millis
	public boolean isIncoming;
	public MessageSentState sentState;
	@Nullable
	public String fileUrl; // in case of "file" message

	public ChatItem(ChatMessage message) {
		this.id = message.id;
		this.content = message.content;
		this.createdAt = message.createdAt;
		this.isIncoming = message.isIncoming;
		this.sentState = message.sentState;
		this.fileUrl = message.fileUrl;
	}

	@Override
	public int compareTo(ChatItem o) {
		return createdAt.compareTo(o.createdAt);
	}

	@NonNull
	public String getId() {
		return id;
	}

	public void setId(@NonNull String id) {
		this.id = id;
	}

	@NonNull
	public String getContent() {
		return content;
	}

	public void setContent(@NonNull String content) {
		this.content = content;
	}

	@NonNull
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(@NonNull Date createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isIncoming() {
		return isIncoming;
	}

	public void setIncoming(boolean incoming) {
		isIncoming = incoming;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ChatItem)) {
			return false;
		}
		ChatItem chatItem = (ChatItem) o;
		return isIncoming == chatItem.isIncoming &&
				id.equals(chatItem.id) &&
				content.equals(chatItem.content) &&
				createdAt.equals(chatItem.createdAt) &&
				sentState == chatItem.sentState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, content, createdAt, isIncoming, sentState);
	}
}
