package ru.livetex.sdk.entity;

import java.util.List;

import androidx.annotation.NonNull;

public final class HistoryEntity extends BaseEntity {
	public static final String TYPE = "history";

	@NonNull
	public String createdAt;
	@NonNull
	public List<GenericMessage> messages;

	@Override
	protected String getType() {
		return TYPE;
	}
}
