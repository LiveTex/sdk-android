package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public abstract class BaseEntity {
	@Nullable
	@SerializedName("correlationId")
	public String correlationId;

	@SerializedName("type")
	public String type;
}
