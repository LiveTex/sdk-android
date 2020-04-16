package ru.livetex.sdk.entity;

import java.util.Date;

/**
 * Server response on message
 */
public final class SentMessage extends BaseEntity {
	public static final String TYPE = "result";

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
