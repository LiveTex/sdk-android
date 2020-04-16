package ru.livetex.sdk.entity;

public final class Department extends BaseEntity {
	public static final String TYPE = "department";

	public String id;

	public Department(String id) {
		this.id = id;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
