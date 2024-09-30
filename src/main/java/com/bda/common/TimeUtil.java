package com.bda.common;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: anran.ma
 * @created: 2024/9/25
 * @description:
 **/
public class TimeUtil {
    public static LocalDate parseDate(String dateStr, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateStr, formatter).toLocalDate();
        } catch (Exception e) {
            // 解析失败，返回 null 或处理错误
            return null;
        }
    }

    public static <T extends ChronoLocalDate> String parseTimeToCommonFormat(T date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public static LocalDate parseDate(String dateStr, DateTimeFormatter dateTimeFormatter) {
        try {
            return LocalDate.parse(dateStr, dateTimeFormatter);
        } catch (Exception e) {
            // 解析失败，返回 null 或处理错误
            return null;
        }
    }

    public static LocalDate regexDate(String dateStr, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dateStr);
        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            return LocalDate.of(year, month, day);
        }
        if (dateStr.contains("小時")) {
            return LocalDate.now();
        }
        if (dateStr.contains("天")) {
            return LocalDate.now().minusDays(Integer.parseInt(dateStr.replace("天", "").trim()));
        }
        return null; // 或处理解析失败的情况
    }
}
