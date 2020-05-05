package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DialogState extends BaseEntity {
	public static final String TYPE = "state";

	@NonNull
	public DialogStatus status = DialogStatus.UNASSIGNED;
	@NonNull
	public EmployeeStatus employeeStatus = EmployeeStatus.OFFLINE;

	@Nullable
	public Employee employee;

	public enum DialogStatus {
		@SerializedName("unassigned")
		UNASSIGNED,
		@SerializedName("inQueue")
		QUEUE,
		@SerializedName("assigned")
		ASSIGNED,
		@SerializedName("aiBot")
		BOT
	}

	public enum EmployeeStatus {
		@SerializedName("online")
		ONLINE,
		@SerializedName("offline")
		OFFLINE
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
