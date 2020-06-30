package ru.livetex.sdk.entity;

public final class RatingEvent extends BaseEntity {
	public static final String TYPE = "rating";

	public int value; // 0 or 1

	public RatingEvent(int value) {
		this.value = value;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
