package ru.livetex.sdk.network;

// Use AuthData instead
@Deprecated
public enum AuthTokenType {
	DEFAULT, // LiveTex token
	CUSTOM // Stable token (or id) in your system, which identifies a unique user
}
