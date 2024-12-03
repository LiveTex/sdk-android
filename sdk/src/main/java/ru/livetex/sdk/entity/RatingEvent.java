package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class RatingEvent extends BaseEntity {
	public static final String TYPE = "rating";

	@NonNull
	public DialogRatingData rate;

	@Nullable
	public String comment = null;

	public RatingEvent(DialogRatingData rate) {
		this.rate = rate;
	}

	public RatingEvent(DialogRatingType type, String rating) {
		this.rate = new DialogRatingData(type, rating);
	}

	public RatingEvent(DialogRatingType type, String rating, String comment) {
		this.rate = new DialogRatingData(type, rating);
		this.comment = comment;
	}

	/**
	 * old system
	 */
	public static RatingEvent createEvent2point(boolean isPositiveFeedback) {
		return new RatingEvent(new DialogRatingData(DialogRatingType.DOUBLE_POINT, isPositiveFeedback ? "1" : "0"));
	}

	/**
	 * old system
	 */
	public static RatingEvent createEvent2point(boolean isPositiveFeedback, @Nullable String comment) {
		return new RatingEvent(DialogRatingType.DOUBLE_POINT, isPositiveFeedback ? "1" : "0", comment);
	}

	/**
	 * new system
	 * @param rating from 1 to 5
	 */
	public static RatingEvent createEvent5points(short rating) {
		return new RatingEvent(DialogRatingType.FIVE_POINT, Short.toString(rating));
	}

	/**
	 * new system
	 * @param rating from 1 to 5
	 */
	public static RatingEvent createEvent5points(short rating, @Nullable String comment) {
		return new RatingEvent(DialogRatingType.FIVE_POINT, Short.toString(rating), comment);
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
