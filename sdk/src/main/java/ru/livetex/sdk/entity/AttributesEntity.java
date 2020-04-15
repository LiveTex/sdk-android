package ru.livetex.sdk.entity;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AttributesEntity extends BaseEntity {
	public static final String TYPE = "attributes";

	@Nullable
	public String name;
	@Nullable
	public String phone;
	@Nullable
	public String email;
	@NonNull
	public final HashMap<String, Object> attributes = new HashMap<>();

	@Override
	protected String getType() {
		return TYPE;
	}
}
