package ru.livetex.sdk.entity;

public final class TypingEvent extends BaseEntity {
	public static final String TYPE = "typing";

	public String content;

	public TypingEvent(String text) {
		super();
		content = text;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
