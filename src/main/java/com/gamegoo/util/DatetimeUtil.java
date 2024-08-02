package com.gamegoo.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DatetimeUtil {

    /**
     * KST 시간대 ISO 8601 형식의 문자열로 변환
     *
     * @param localDateTime
     * @return
     */
    public static String toKSTString(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return zonedDateTime.format(formatter);
    }
}
