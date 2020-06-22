package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

public enum LiveTexError {
	@SerializedName("systemUnavailable")
	SYSTEM_UNAVAILABLE, // server-side error
	@SerializedName("departmentNoId")
	NO_DEPARTMENT_ID,
	@SerializedName("departmentInvalidId")
	INVALID_DEPARTMENT,
	@SerializedName("attributesWrongFormat")
	ATTRIBUTES_WRONG_FORMAT, // wrong format of attributes HashMap in AttributesEntity
	@SerializedName("textContentIsEmpty")
	EMPTY_MESSAGE,
	@SerializedName("fileNoName")
	FILE_NO_NAME,
	@SerializedName("fileNoUrl")
	FILE_NO_URL,
}
