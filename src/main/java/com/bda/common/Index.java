package com.bda.common;

import com.bda.stock.WebCrawlerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: anran.ma
 * @created: 2024/9/13
 * @description:
 **/
@Data
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

    public Index(String name, String price, String timestamp, String preClose, String open, String ratio, String amount, String avgPrice, String increase,String format) {
        this.name = name;
        this.price = price;
        this.timestamp = timestamp;
        this.preClose = preClose;
        this.open = open;
        this.ratio = ratio;
        this.amount = amount;
        this.avgPrice = avgPrice;
        this.increase = increase;
        this.time = WebCrawlerUtils.patternTime(timestamp, format);
    }

    public Index(String name, String price, String timestamp, String increase, String ratio, String format) {
        this.name = name;
        this.price = price;
        this.timestamp = timestamp;
        this.increase = increase;
        this.ratio = ratio;
        this.time = WebCrawlerUtils.patternTime(timestamp, format);
    }

    public Index(String name, String timestamp, String open, String close, String amount, String increase, String ratio, String high, String low,String preClose,String price,String format) {
        this.name = name;
        this.timestamp = timestamp;
        this.open = open;
        this.close = close;
        this.amount = amount;
        this.increase = increase;
        this.ratio = ratio;
        this.high = high;
        this.low = low;
        this.preClose = preClose;
        this.price = price;
        this.time = WebCrawlerUtils.patternTime(timestamp, format);
    }


}
