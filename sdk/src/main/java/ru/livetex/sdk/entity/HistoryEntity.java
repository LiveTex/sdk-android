package ru.livetex.sdk.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

public final class HistoryEntity extends BaseEntity {
	public static final String TYPE = "update";

	@NonNull
	public Date createdAt;
	@NonNull
	public List<GenericMessage> messages = new ArrayList<>();

	@Override
	protected String getType() {
		return TYPE;
	}
}
