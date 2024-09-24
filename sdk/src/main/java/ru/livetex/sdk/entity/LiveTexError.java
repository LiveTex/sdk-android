package ru.livetex.sdk.entity;

import com.google.gson.annotations.SerializedName;

public enum LiveTexError {
	@SerializedName("systemUnavailable")
	SYSTEM_UNAVAILABLE, // server-side error
	@SerializedName("fileNoName")
	FILE_NO_NAME,
	@SerializedName("fileNoUrl")
	FILE_NO_URL,
	@SerializedName("textContentIsEmpty")
	EMPTY_MESSAGE,
	@SerializedName("attributesWrongFormat")
	ATTRIBUTES_WRONG_FORMAT, // wrong format of attributes HashMap in AttributesEntity
	@SerializedName("departmentNoId")
	NO_DEPARTMENT_ID,
	@SerializedName("departmentInvalidId")
	INVALID_DEPARTMENT,
	@SerializedName("historyFromMessageIdNotDefined")
	INVALID_HISTORY_PARAMS,
	@SerializedName("buttonPayloadIsEmpty")
	BUTTON_PAYLOAD_IS_EMPTY,
	@SerializedName("ratingRateIsEmpty")
	RATING_RATE_IS_EMPTY,
	@SerializedName("ratingIncorrectRateType")
	RATING_INCORRECT_RATE_TYPE,
	@SerializedName("ratingUnavailable")
	RATING_UNAVAILABLE,
}
