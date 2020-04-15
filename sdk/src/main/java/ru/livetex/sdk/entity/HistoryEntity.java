package ru.livetex.sdk.entity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public final class HistoryEntity extends BaseEntity {
	public static final String TYPE = "history";

	@NonNull
	public String createdAt;
	@NonNull
	public transient List<GenericMessage> messages = new ArrayList<>();

	@Override
	protected String getType() {
		return TYPE;
	}
}
