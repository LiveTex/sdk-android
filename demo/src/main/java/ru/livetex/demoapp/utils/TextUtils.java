package ru.livetex.demoapp.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import androidx.annotation.Nullable;

public final class TextUtils {

	public static Spannable applyLinks(String source) {
		Spanned text = new SpannableString(source);
		SpannableString buffer = new SpannableString(text);
		Linkify.addLinks(buffer, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
		return buffer;
	}

	@Nullable
	public static String getFirstLink(Spannable source) {
		URLSpan[] spans = source.getSpans(0, source.length(), URLSpan.class);
		return spans != null && spans.length > 0 ? spans[0].getURL() : null;
	}
}
