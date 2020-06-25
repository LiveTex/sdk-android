package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public final class Department extends BaseEntity implements Comparable<Department> {
	public static final String TYPE = "department";

	@NonNull
	public String id;
	@NonNull
	public String name;
	public int order;

	// for sending from client
	public Department(@NonNull String id) {
		this.id = id;
	}

	@Override
	protected String getType() {
		return TYPE;
	}

	@Override
	public int compareTo(Department o) {
		return order - o.order;
	}
}
