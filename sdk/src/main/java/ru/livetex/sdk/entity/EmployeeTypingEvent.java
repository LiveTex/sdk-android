package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public final class EmployeeTypingEvent extends BaseEntity {
	public static final String TYPE = "employeeTyping";

	@NonNull
	public String createdAt;

	@Override
	protected String getType() {
		return TYPE;
	}
}
