package ru.livetex.sdk.entity;

public final class RatingEvent extends BaseEntity {
	public static final String TYPE = "rating";

	public String value; // "0" or "1"

	public RatingEvent(String value) {
		this.value = value;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
