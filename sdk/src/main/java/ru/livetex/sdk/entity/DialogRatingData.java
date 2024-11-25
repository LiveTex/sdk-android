package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogRatingData {
	@NonNull
	public DialogRatingType type;

	@NonNull
	// fivePoint: "1" | "2" | "3" | "4" | "5".
	// doublePoint: "0" | "1"
	public String value;

	@Nullable
	public String comment;

	public DialogRatingData(@NonNull DialogRatingType type, @NonNull String value, @Nullable String comment) {
		this.type = type;
		this.value = value;
		this.comment = comment;
	}
}
