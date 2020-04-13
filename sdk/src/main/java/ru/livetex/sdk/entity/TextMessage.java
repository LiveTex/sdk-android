package ru.livetex.sdk.entity;

public final class TextMessage extends BaseEntity {
	public static final String TYPE = "text";

	public String content;

	public TextMessage(String text) {
		super();
		this.content = text;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
