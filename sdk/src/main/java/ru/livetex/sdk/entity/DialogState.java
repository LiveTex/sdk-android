package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DialogState extends BaseEntity {
	public static final String TYPE = "state";

	@NonNull
	public DialogStatus status;
	@Nullable
	public Employee employee;

	public enum DialogStatus {
		@SerializedName("unassigned")
		UNASSIGNED(),
		@SerializedName("inQueue")
		QUEUE(),
		@SerializedName("assigned")
		ASSIGNED(),
		@SerializedName("aiBot")
		BOT();
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
