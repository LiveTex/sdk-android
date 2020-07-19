package ru.livetex.sdk.entity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Action buttons that can be included in TextMessage
 */
public class KeyboardEntity {

	public static class Button {
		@NonNull
		public String label;
		@NonNull
		public String payload;
		@Nullable
		public String url;
	}

	@NonNull
	public List<Button> buttons;
	// Some button was already pressed, user can no longer press buttons on this Keyboard
	public boolean pressed;
}
