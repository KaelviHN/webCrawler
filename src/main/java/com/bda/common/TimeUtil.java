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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            // 解析失败，返回 null 或处理错误
           return null;
        }
    }

    public static <T extends ChronoLocalDate> String parseTimeToCommonFormat(T date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
}
