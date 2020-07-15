package ru.livetex.sdk.entity;

import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;

public final class FileMessage extends BaseEntity implements GenericMessage {
	public static final String TYPE = "file";

	@NonNull
	public String id;
	@NonNull
	public String name;
	@NonNull
	public String url;
	@NonNull
	public Date createdAt; // timestamp in millis
	@NonNull
	public transient Creator creator;

	public FileMessage(FileUploadedResponse response) {
		super();
		this.id = UUID.randomUUID().toString();
		this.name = response.name;
		this.url = response.url;
		this.createdAt = new Date();
		this.creator = new Visitor();
	}

	@Override
	@NonNull
	public Creator getCreator() {
		return creator;
	}

	@Override
	public void setCreator(@NonNull Creator creator) {
		this.creator = creator;
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
