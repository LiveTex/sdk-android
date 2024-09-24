package ru.livetex.sdk.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Employee implements Creator {
	public static final String TYPE = "employee";

	@NonNull
	public String name;
	@Nullable
	public String position;
	@Nullable
	public String avatarUrl;
	// Old (2 scores) rating system. null means no feedback done.
	@Deprecated
	@Nullable
	public String rating;
}
