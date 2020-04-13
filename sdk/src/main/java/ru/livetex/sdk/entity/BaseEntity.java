package ru.livetex.sdk.entity;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseEntity {
	@Nullable
	@SerializedName("correlationId")
	public String correlationId;

	@NonNull
	@SerializedName("type")
	public String type;

	// todo: object
	@Nullable
	@SerializedName("error")
	public Object[] error;

	public BaseEntity() {
		correlationId = UUID.randomUUID().toString();
		this.type = getType();
	}

	// Force to define type
	protected abstract String getType();
}
