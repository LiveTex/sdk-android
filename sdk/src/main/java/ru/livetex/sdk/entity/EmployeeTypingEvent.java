package ru.livetex.sdk.entity;

import java.util.Date;

import androidx.annotation.NonNull;

public final class EmployeeTypingEvent extends BaseEntity {
	public static final String TYPE = "employeeTyping";

	@NonNull
	public Date createdAt;

	@Override
	protected String getType() {
		return TYPE;
	}
}
