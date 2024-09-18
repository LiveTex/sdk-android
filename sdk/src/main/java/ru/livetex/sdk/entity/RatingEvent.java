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

	/**
	 * old system
	 */
	public static RatingEvent createEvent2point(boolean isPositiveFeedback) {
		return new RatingEvent(new DialogRatingData(DialogRatingType.DOUBLE_POINT, isPositiveFeedback ? "1" : "0"));
	}

	/**
	 * new system
	 * @param rating from 1 to 5
	 */
	public static RatingEvent createEvent5points(short rating) {
		return new RatingEvent(new DialogRatingData(DialogRatingType.FIVE_POINT, Short.toString(rating)));
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
