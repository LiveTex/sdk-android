package ru.livetex.sdk.entity;

import java.util.Date;

import androidx.annotation.Nullable;

/**
 * Server response on most requests. Request-reponse linked via correlationId field.
 */
public final class ResponseEntity extends BaseEntity {
	public static final String TYPE = "result";

	@Nullable
	public SentMessageBody sentMessage;

	@Override
	protected String getType() {
		return TYPE;
	}

	public static class SentMessageBody {
		public Date createdAt;
		public String id;
	}
}
