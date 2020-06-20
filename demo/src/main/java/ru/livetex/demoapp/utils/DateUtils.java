package ru.livetex.demoapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

	private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

	private DateUtils() {}

	public static synchronized String timestampToTime(Date date) {
		return sdf.format(date);
	}
}
