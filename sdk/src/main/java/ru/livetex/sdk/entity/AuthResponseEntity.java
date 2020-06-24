package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;

public final class AuthResponseEntity {
	public static final String TYPE = "attributes";

	@NonNull
	public String userToken;
	@NonNull
	public Endpoints endpoints;

	public static class Endpoints {
		@NonNull
		public String ws;
		@NonNull
		public String upload;
	}
}