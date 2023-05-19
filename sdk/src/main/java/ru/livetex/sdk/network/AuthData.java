package ru.livetex.sdk.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AuthData {
	@Nullable
	public final String visitorToken;

	@Nullable
	public final String customVisitorToken;

	private AuthData(@Nullable String visitorToken,
					@Nullable String customVisitorToken) {
		this.visitorToken = visitorToken;
		this.customVisitorToken = customVisitorToken;
	}

	/**
	 * Common case.
	 * @param visitorToken - token which identifies a current user. Should be null if user is new.
	 */
	public static AuthData withVisitorToken(@Nullable String visitorToken) {
		return new AuthData(visitorToken, null);
	}

	/**
	 * When you want to use your users system with unique ids.
	 * @param customVisitorToken - some unique token (or id) which identifies a current user in your system. Can't be null.
	 */
	public static AuthData withCustomVisitorToken(@NonNull String customVisitorToken) {
		return new AuthData(null, customVisitorToken);
	}

	/**
	 * Special case - when you want to preserve user previous data with visitorToken and continue to work with your users system.
	 * @param visitorToken - token which were used to identify a user in past. Can't be null here.
	 * @param customVisitorToken - some unique token (or id) which now identifies a current user in your system. Can't be null.
	 */
	public static AuthData withVisitorAndCustomTokens(@NonNull String visitorToken, @NonNull String customVisitorToken) {
		return new AuthData(visitorToken, customVisitorToken);
	}
}
