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
	// Indicates when chat input UI should be shown or hidden/disabled. When false, user shouldn't be able to send text or file messages.
	public boolean inputEnabled;

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
