package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public class DialogRatingData {
	@NonNull
	public DialogRatingType type;

	@NonNull
	// fivePoint: "1" | "2" | "3" | "4" | "5".
	// doublePoint: "0" | "1"
	public String value;

	public DialogRatingData(@NonNull DialogRatingType type, @NonNull String value) {
		this.type = type;
		this.value = value;
	}
}
