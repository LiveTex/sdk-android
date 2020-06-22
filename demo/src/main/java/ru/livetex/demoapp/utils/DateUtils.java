package ru.livetex.demoapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

	private final static SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
	private final static SimpleDateFormat sdfDay = new SimpleDateFormat("dd EEEE", Locale.getDefault());

	private DateUtils() {}

	public static synchronized String dateToTime(Date date) {
		return sdfTime.format(date);
	}

	public static synchronized String dateToDay(Date date) {
		return sdfDay.format(date);
	}
}