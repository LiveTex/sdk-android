package ru.livetex.demoapp.ui.adapter;

import java.util.Date;

import androidx.annotation.NonNull;
import ru.livetex.demoapp.db.entity.ChatMessage;

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

	public ChatItem(ChatMessage message) {
		this.id = message.id;
		this.content = message.content;
		this.createdAt = message.createdAt;
		this.isIncoming = message.isIncoming;
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
}
