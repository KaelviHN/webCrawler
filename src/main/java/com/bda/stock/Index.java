package com.bda.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: anran.ma
 * @created: 2024/9/13
 * @description:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Index {
    /**
     * 名称
     */
    private String name;
    /**
     * 价格
     */
    private String price;
    /**
     * 毫秒值
     */
    private String timestamp;
    /**
     * 昨日收盘价
     */
    private String preClose;
    /**
     * 今日开盘价
     */
    private String open;
    /**
     * 最高
     */
    private String high;
    /**
     * 最低
     */
    private String low;
    /**
     * 涨跌额
     */
    private String ratio;

    /**
     * 成交额
     */
    private String amount;

    /**
     * 均价
     */
    private String avgPrice;
    /**
     * 涨跌额
     */
    private String increase;

    /**
     * 今日收盘价
     */
    private String close;
    /**
     * 格式化后的时间
     */
    private String time;

    /**
     * 振幅
     */
    private String amplitudeRatio;

    /**
     * 总成交额
     */
    private String totalAmount;

    public static String patternTime(String timestamp, String format) {
        ZonedDateTime dateTime = Instant.ofEpochSecond(Long.parseLong(timestamp)).atZone(ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }


}
