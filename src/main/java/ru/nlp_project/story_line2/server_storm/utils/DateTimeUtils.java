package ru.nlp_project.story_line2.server_storm.utils;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateTimeUtils {
	private static ZoneId defaultZoneId;
	private static DateTimeFormatter formatter;

	static {
		defaultZoneId = ZoneId.systemDefault();
		formatter = DateTimeFormatter.ISO_INSTANT.withZone(defaultZoneId);
	}

	public static Date deserializeDate(String value) throws DateTimeParseException {
		if (null == value)
			return null;
		// 1970-01-01T00:00:01Z or 1970-01-01T00:00:00.001Z
		ZonedDateTime zdt = ZonedDateTime.parse(value, formatter);
		return Date.from(zdt.toInstant());

	}

	public static String serializeDate(Date value) throws DateTimeException {
		Instant instant = value.toInstant();
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
		return formatter.format(zonedDateTime);
	}

	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date asDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate asLocalDate(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static LocalDateTime asLocalDateTime(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}
}
