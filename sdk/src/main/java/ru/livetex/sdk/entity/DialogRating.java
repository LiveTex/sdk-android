package ru.livetex.sdk.entity;

import androidx.annotation.Nullable;

public class DialogRating {

	// null when it's not allowed to rate
	@Nullable
	public DialogRatingType enabledType;

	// null if dialog isn't rated yet
	@Nullable
	public DialogRatingData isSet;

}
