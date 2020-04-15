package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public final class FileMessage extends BaseEntity implements GenericMessage {
	public static final String TYPE = "file";

	@NonNull
	public String name;
	@NonNull
	public String url;
	@NonNull
	public transient Creator creator;

	@Override
	protected String getType() {
		return TYPE;
	}
}
