package ru.livetex.sdk.entity;

import java.util.HashMap;
import java.util.Map;

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

	public AttributesEntity(@Nullable String name, @Nullable String phone, @Nullable String email, @Nullable Map<String, Object> attrs) {
		super();
		this.name = name;
		this.phone = phone;
		this.email = email;
		if (attrs != null) {
			attributes.putAll(attrs);
		}
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
