package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

public enum LiveTexError {
	@SerializedName("departmentInvalidId")
	INVALID_DEPARTMENT,
	@SerializedName("textContentIsEmpty")
	EMPTY_MESSAGE
}
