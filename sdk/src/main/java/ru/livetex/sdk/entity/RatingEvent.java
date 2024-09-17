package ru.livetex.sdk.entity;

public final class RatingEvent extends BaseEntity {
	public static final String TYPE = "rating";

	public DialogRatingData rate;

	public RatingEvent(DialogRatingData rate) {
		this.rate = rate;
	}

	public RatingEvent(DialogRatingType type, String rating) {
		this.rate = new DialogRatingData(type, rating);
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
