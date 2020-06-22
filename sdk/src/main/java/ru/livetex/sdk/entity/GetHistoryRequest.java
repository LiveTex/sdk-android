package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

/**
 * Request previous chat messages. Response with messages will come as HistoryEntity event.
 */
public final class GetHistoryRequest extends BaseEntity {
	public static final String TYPE = "getHistory";

	// First known message in client history. Previous messages requested before this one.
	@NonNull
	public String messageId;
	// Count of messages before messageId
	public int offset;

	public GetHistoryRequest(@NonNull String messageId, int offset) {
		this.messageId = messageId;
		this.offset = offset;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
