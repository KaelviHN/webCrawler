package com.bda.common;

import java.sql.Time;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author: anran.ma
 * @created: 2024/9/25
 * @description:
 **/
public class TimeUtil {
    public static LocalDate parseDate(String dateStr,String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            // 解析失败，返回 null 或处理错误
           return null;
        }
    }

    public static <T extends ChronoLocalDate> String parseTimeToCommonFormat(T date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public static LocalDate parseDate(String dateStr,DateTimeFormatter dateTimeFormatter){
        try {
            return LocalDate.parse(dateStr, dateTimeFormatter);
        } catch (Exception e) {
            // 解析失败，返回 null 或处理错误
            return null;
        }
    }
}
