package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

public final class DialogState extends BaseEntity {
	public static final String TYPE = "state";

	public DialogStatus status;
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
}
