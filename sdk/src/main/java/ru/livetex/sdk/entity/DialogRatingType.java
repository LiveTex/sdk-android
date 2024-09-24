package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

public enum DialogRatingType {
	// old "good-bad" rating system
	@SerializedName("doublePoint")
	DOUBLE_POINT,
	// new "five stars" rating system
	@SerializedName("fivePoint")
	FIVE_POINT,
}
