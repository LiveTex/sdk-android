package ru.livetex.sdk.entity;

import java.util.List;

import androidx.annotation.NonNull;

public final class DepartmentRequestEntity extends BaseEntity {
	public static final String TYPE = "departmentRequest";

	@NonNull
	public List<Department> departments;

	@Override
	protected String getType() {
		return TYPE;
	}
}
