package com.bda.stock;

import com.bda.common.FileUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: anran.ma
 * @created: 2024/9/12
 * @description:
 **/
public class WebCrawlerUtils {
    public static final String HK = "港元";
    public static final String MAC = "澳门元";
    public static final String HSI = "恒生指数";
    public static final String HK_PREFIX = "hong_kong_dollar_exchange";
    public static final String MAC_PREFIX = "macao_dollar_exchange_rate";
    public static final String HSI_PREFIX = "hangseng_index";
    public static final String SUFFIX = ".json";
    public static final String SECOND_PREFIX = "second_";
    public static final String MINUTE_PREFIX = "minute_";
    public static final String DAY_PREFIX = "day_";
    public static final String FIVE_DAY_PREFIX = "five_day_";
    public static final String WEEK_PREFIX = "week_";
    public static final String MONTH_PREFIX = "month_";
    public static final String QUARTER_PREFIX = "quarter_";
    public static final String HEADER_PREFIX = "header_";
    public static final String YEAR_PREFIX = "year_";
    public static final String MINUTE_FORMAT = "yyyyMMddHHmm";
    public static final String SECOND_FORMAT = "yyyyMMddHHmmss";
    public static final String DAY_FORMAT = "yyyyMMdd";
    private static final Log log = LogFactory.getLog(WebCrawlerUtils.class);

    public static String hsiTime = "";
    public static String hkTime = "";
    public static String macTime = "";

    public static String hsiRealTime = "";
    public static String hkRealTime = "";
    public static String macRealTime = "";

    public static void realTask(String jarPath) {
        Instant now = Instant.now();
        String time = getTime(DAY_FORMAT);
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_minute&need_reverse_real=1&code=HKDCNY&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_minute&need_reverse_real=1&code=MOPCNY&finClientType=pc";
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_minute&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&finClientType=pc";
        String hk = loadJson(urlHK);
        String mac = loadJson(urlMAC);
        String hsi = loadJson(urlHSI);
        Index hkIdx = parseExchange(hk, HK);
        Index macIdx = parseExchange(mac, MAC);
        Index hsiIdx = parseStock(hsi, HSI);
        if (hkRealTime.isEmpty()) {
            hkRealTime = time;
        }
        if (isClose(hkIdx.getTimestamp(), now))
            FileUtil.appendJsonToFile(SECOND_PREFIX + HK_PREFIX + "_" + hkRealTime + SUFFIX, jarPath, hkIdx);
        if (macRealTime.isEmpty()) {
            macRealTime = time;
        }
        if (isClose(macIdx.getTimestamp(), now))
            FileUtil.appendJsonToFile(SECOND_PREFIX + MAC_PREFIX + "_" + macRealTime + SUFFIX, jarPath, macIdx);
        if (hsiRealTime.isEmpty()) {
            hsiRealTime = time;
        }
        if (isClose(hsiIdx.getTimestamp(), now))
            FileUtil.appendJsonToFile(SECOND_PREFIX + HSI_PREFIX + "_" + hsiRealTime + SUFFIX, jarPath, hsiIdx);
    }

    public static void minute(String path) {
        String time = getTime(MINUTE_FORMAT);
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_minute&need_reverse_real=1&code=HKDCNY&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_minute&need_reverse_real=1&code=MOPCNY&finClientType=pc";
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_minute&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&finClientType=pc";
        String hk = loadJson(urlHK);
        String mac = loadJson(urlMAC);
        String hsi = loadJson(urlHSI);
        List<Index> indicesHK = parseK(hk, HK, MINUTE_FORMAT, true);
        List<Index> indicesMac = parseK(mac, MAC, MINUTE_FORMAT, true);
        List<Index> indicesHSI = parseK(hsi, HSI, MINUTE_FORMAT, true);
        Index hkHeader = parseExchange(hk, HK);
        Index macHeader = parseExchange(mac, MAC);
        Index hsiHeader = parseStock(hsi, HSI);
        if (!CollectionUtils.isEmpty(indicesHK) && !hkTime.equals(indicesHK.get(indicesHK.size() - 1).getTimestamp())) {
            Index index = indicesHK.get(indicesHK.size() - 1);
            hkTime = index.getTimestamp();
            hkHeader.setTimestamp(hkRealTime);
            hkHeader.setTime(index.getTime());
            FileUtil.writeHistory(path, indicesHK, MINUTE_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
            FileUtil.cover(path, hkHeader, HEADER_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
        }
        if (!CollectionUtils.isEmpty(indicesMac) && !macTime.equals(indicesMac.get(indicesMac.size() - 1).getTimestamp())) {
            Index index = indicesMac.get(indicesMac.size() - 1);
            macTime = index.getTimestamp();
            macHeader.setTimestamp(macTime);
            macHeader.setTime(index.getTime());
            FileUtil.writeHistory(path, indicesMac, MINUTE_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
            FileUtil.cover(path, macHeader, HEADER_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
        }

        if (!CollectionUtils.isEmpty(indicesHSI) && !hsiTime.equals(indicesHSI.get(indicesHSI.size() - 1).getTimestamp())) {
            Index index = indicesHSI.get(indicesHSI.size() - 1);
            hsiTime = index.getTimestamp();
            hsiHeader.setTimestamp(hsiTime);
            hsiHeader.setTime(index.getTime());
            FileUtil.writeHistory(path, indicesHSI, MINUTE_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
            FileUtil.cover(path, hsiHeader, HEADER_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }

    public static void day(String path) {
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_kline&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&is_kc=0&ktype=day&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=day&code=MOPCNY&finClientType=pc";
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=day&code=HKDCNY&finClientType=pc";
        String hsiIdx = loadJson(urlHSI);
        String macIdx = loadJson(urlMAC);
        String hkIdx = loadJson(urlHK);
        List<Index> indicesHSI = parseK(hsiIdx, HSI, DAY_FORMAT, false);
        List<Index> indicesMac = parseK(macIdx, MAC, DAY_FORMAT, false);
        List<Index> indicesHK = parseK(hkIdx, HK, DAY_FORMAT, false);
        if (!CollectionUtils.isEmpty(indicesHK)) {
            String time = Index.patternTime(indicesHK.get(indicesHK.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHK, DAY_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
        }
        if (!CollectionUtils.isEmpty(indicesMac)) {
            String time = Index.patternTime(indicesMac.get(indicesMac.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesMac, DAY_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
        }

        if (!CollectionUtils.isEmpty(indicesHSI)) {
            String time = Index.patternTime(indicesHSI.get(indicesHSI.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHSI, DAY_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }

    public static void fiveDay(String path) {
        String urlHSI = "https://finance.pae.baidu.com/selfselect/getstockquotation?all=1&code=HSI&isIndex=true&isBk=false&isBlock=false&isFutures=false&isStock=false&newFormat=1&market_type=hk&group=quotation_index_fiveday&finClientType=pc";
        String hsiIdx = loadJson(urlHSI);
        List<Index> indicesHSI = parseFiveDay(hsiIdx, HSI);
        if (!CollectionUtils.isEmpty(indicesHSI)) {
            String time = Index.patternTime(indicesHSI.get(indicesHSI.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHSI, FIVE_DAY_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }

    public static void week(String path) {
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_kline&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&is_kc=0&ktype=week&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=week&code=MOPCNY&finClientType=pc";
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=week&code=HKDCNY&finClientType=pc";
        String hsiIdx = loadJson(urlHSI);
        String macIdx = loadJson(urlMAC);
        String hkIdx = loadJson(urlHK);
        List<Index> indicesHSI = parseK(hsiIdx, HSI, DAY_FORMAT, false);
        List<Index> indicesMac = parseK(macIdx, MAC, DAY_FORMAT, false);
        List<Index> indicesHK = parseK(hkIdx, HK, DAY_FORMAT, false);
        if (!CollectionUtils.isEmpty(indicesHK)) {
            String time = Index.patternTime(indicesHK.get(indicesHK.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHK, WEEK_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
        }
        if (!CollectionUtils.isEmpty(indicesMac)) {
            String time = Index.patternTime(indicesMac.get(indicesMac.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesMac, WEEK_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
        }

        if (!CollectionUtils.isEmpty(indicesHSI)) {
            String time = Index.patternTime(indicesHSI.get(indicesHSI.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHSI, WEEK_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }

    public static void month(String path) {
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_kline&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&is_kc=0&ktype=month&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=month&code=MOPCNY&finClientType=pc";
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=month&code=HKDCNY&finClientType=pc";
        String hsiIdx = loadJson(urlHSI);
        String macIdx = loadJson(urlMAC);
        String hkIdx = loadJson(urlHK);
        List<Index> indicesHSI = parseK(hsiIdx, HSI, DAY_FORMAT, false);
        List<Index> indicesMac = parseK(macIdx, MAC, DAY_FORMAT, false);
        List<Index> indicesHK = parseK(hkIdx, HK, DAY_FORMAT, false);
        if (!CollectionUtils.isEmpty(indicesHK)) {
            String time = Index.patternTime(indicesHK.get(indicesHK.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHK, MONTH_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
        }
        if (!CollectionUtils.isEmpty(indicesMac)) {
            String time = Index.patternTime(indicesMac.get(indicesMac.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesMac, MONTH_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
        }

        if (!CollectionUtils.isEmpty(indicesHSI)) {
            String time = Index.patternTime(indicesHSI.get(indicesHSI.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHSI, MONTH_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }


    public static void quarter(String path) {
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_kline&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&is_kc=0&ktype=quarter&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=quarter&code=MOPCNY&finClientType=pc";
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=quarter&code=HKDCNY&finClientType=pc";
        String hsiIdx = loadJson(urlHSI);
        String macIdx = loadJson(urlMAC);
        String hkIdx = loadJson(urlHK);
        List<Index> indicesHSI = parseK(hsiIdx, HSI, DAY_FORMAT, false);
        List<Index> indicesMac = parseK(macIdx, MAC, DAY_FORMAT, false);
        List<Index> indicesHK = parseK(hkIdx, HK, DAY_FORMAT, false);
        if (!CollectionUtils.isEmpty(indicesHK)) {
            String time = Index.patternTime(indicesHK.get(indicesHK.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHK, QUARTER_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
        }
        if (!CollectionUtils.isEmpty(indicesMac)) {
            String time = Index.patternTime(indicesMac.get(indicesMac.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesMac, QUARTER_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
        }

        if (!CollectionUtils.isEmpty(indicesHSI)) {
            String time = Index.patternTime(indicesHSI.get(indicesHSI.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHSI, QUARTER_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }


    public static void year(String path) {
        String urlHSI = "https://finance.pae.baidu.com/vapi/v1/getquotation?srcid=5353&all=1&pointType=string&group=quotation_index_kline&query=HSI&code=HSI&market_type=hk&newFormat=1&name=%E6%81%92%E7%94%9F%E6%8C%87%E6%95%B0&is_kc=0&ktype=year&finClientType=pc";
        String urlMAC = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=year&code=MOPCNY&finClientType=pc";
        String urlHK = "https://finance.pae.baidu.com/vapi/v1/getquotation?group=huilv_kline&ktype=year&code=HKDCNY&finClientType=pc";
        String hsiIdx = loadJson(urlHSI);
        String macIdx = loadJson(urlMAC);
        String hkIdx = loadJson(urlHK);
        List<Index> indicesHSI = parseK(hsiIdx, HSI, DAY_FORMAT, false);
        List<Index> indicesMac = parseK(macIdx, MAC, DAY_FORMAT, false);
        List<Index> indicesHK = parseK(hkIdx, HK, DAY_FORMAT, false);
        if (!CollectionUtils.isEmpty(indicesHK)) {
            String time = Index.patternTime(indicesHK.get(indicesHK.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHK, YEAR_PREFIX + HK_PREFIX + "_" + time + SUFFIX);
        }
        if (!CollectionUtils.isEmpty(indicesMac)) {
            String time = Index.patternTime(indicesMac.get(indicesMac.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesMac, YEAR_PREFIX + MAC_PREFIX + "_" + time + SUFFIX);
        }

        if (!CollectionUtils.isEmpty(indicesHSI)) {
            String time = Index.patternTime(indicesHSI.get(indicesHSI.size() - 1).getTimestamp(), DAY_FORMAT);
            FileUtil.writeHistory(path, indicesHSI, YEAR_PREFIX + HSI_PREFIX + "_" + time + SUFFIX);
        }
    }


    public static String loadJson(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlObject.openConnection();
            String cookie = "BIDUPSID=2A1DC0562D259DA524E64A163281F50C; PSTM=1718348657; BAIDUID=5FF070B9BF2D93CF2701C1196215F8BB:FG=1; BDUSS=0VpS3ZvQ35YbzJxTThZUkk0R1UxOWlzQTZkdG1McG1ER1RjbXRwR1k4WWFxcjVtSVFBQUFBJCQAAAAAAQAAAAEAAABL~lg~QW5laWRhU3dvcmQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABodl2YaHZdmN; BDUSS_BFESS=0VpS3ZvQ35YbzJxTThZUkk0R1UxOWlzQTZkdG1McG1ER1RjbXRwR1k4WWFxcjVtSVFBQUFBJCQAAAAAAQAAAAEAAABL~lg~QW5laWRhU3dvcmQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABodl2YaHZdmN; H_WISE_SIDS_BFESS=60449_60359_60681_60721_60725_60747; H_PS_PSSID=60449_60721_60725_60360_60748_60732; H_WISE_SIDS=60449_60721_60725_60360_60748_60732; BA_HECTOR=0120008k8101858h0l2ka1a10k41q91jel14c1u; BAIDUID_BFESS=5FF070B9BF2D93CF2701C1196215F8BB:FG=1; ZFY=JJU2O8qBQuVor02HvmzqfxMTiH1sGqNwY:BsILOEuMD4:C";
            httpURLConnection.setRequestProperty("Cookie", cookie);
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @SneakyThrows
    public static List<Index> parseK(String json, String name, String pattern, boolean needHeader) {
        List<Index> datas = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode resultNode = objectMapper.readTree(json).path("Result").path("newMarketData");
        if (resultNode == null) return datas;
        // 获取表头
        JsonNode keys = resultNode.path("keys");
        Map<String, Integer> idxMap = new HashMap<>();
        if (keys == null || !keys.isArray()) return datas;
        Iterator<JsonNode> elements = keys.elements();
        int idx = 0;
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            String key = next.asText().replace("\"", "");
            idxMap.put(key, idx++);
        }
        // 获取数据
        JsonNode marketData = resultNode.path("marketData");
        if (marketData == null) return datas;
        // 处理分k
        if (marketData.isArray()) {
            Iterator<JsonNode> iterator = marketData.elements();
            if (iterator.hasNext()) marketData = iterator.next().get("p");
        }
        String[] dayDatas = marketData.asText().split(";");
        for (String dayData : dayDatas) {
            List<String> data = Arrays.asList(dayData.split(","));
            String timestamp = data.get(idxMap.get("timestamp"));
            String open = idxMap.get("open") != null ? data.get(idxMap.get("open")) : null;
            String close = idxMap.get("close") != null ? data.get(idxMap.get("close")) : null;
            String amount = idxMap.get("amount") != null ? data.get(idxMap.get("amount")) : null;
            String ratio = idxMap.get("ratio") != null ? data.get(idxMap.get("ratio")) : null;
            String increase = idxMap.get("range") != null ? data.get(idxMap.get("range")) : null;
            String preClose = idxMap.get("preClose") != null ? data.get(idxMap.get("preClose")) : null;
            String high = idxMap.get("high") != null ? data.get(idxMap.get("high")) : null;
            String low = idxMap.get("low") != null ? data.get(idxMap.get("low")) : null;
            String price = idxMap.get("price") != null ? data.get(idxMap.get("price")) : null;
            Index index = Index.builder()
                    .timestamp(timestamp).open(open)
                    .close(close).amount(amount)
                    .ratio(ratio).increase(increase)
                    .preClose(preClose).high(high)
                    .low(low).price(price)
                    .time(Index.patternTime(timestamp, pattern)).name(name)
                    .build();
            datas.add(needHeader ? parseHeader(json, name, index) : index);
        }
        return datas;
    }

    public static String getTime(String pattern) {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(format);
    }


    @SneakyThrows
    public static Index parseExchange(String json, String idx) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json).path("Result");
        if (rootNode.isMissingNode()) return new Index();
        String price = rootNode.get("cur").get("price").asText();
        String timestamp = rootNode.get("update").get("time").asText();
        String ratio = rootNode.get("cur").get("ratio").asText();
        String increase = rootNode.get("cur").get("increase").asText();
        Index index = Index.builder()
                .name(idx).price(price)
                .timestamp(timestamp).ratio(ratio)
                .increase(increase).time(Index.patternTime(timestamp, SECOND_FORMAT))
                .build();
        return parseHeader(json, idx, index);
    }

    @SneakyThrows
    public static Index parseStock(String json, String idx) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode curData = objectMapper.readTree(json).path("Result").path("cur");
        if (curData.isMissingNode()) return new Index();
        String price = Optional.ofNullable(curData.get("price"))
                .map(JsonNode::asText)
                .orElse(null);
        String timestamp = Optional.ofNullable(curData.get("time"))
                .map(JsonNode::asText)
                .orElse(null);
        String avgPrice = Optional.ofNullable(curData.get("avgPrice"))
                .map(JsonNode::asText)
                .orElse(null);
        String increase = Optional.ofNullable(curData.get("increase"))
                .map(JsonNode::asText)
                .orElse(null);
        String ratio = Optional.ofNullable(curData.get("ratio"))
                .map(JsonNode::asText)
                .orElse(null);
        String amount = Optional.ofNullable(curData.get("amount"))
                .map(JsonNode::asText)
                .orElse(null);
        JsonNode update = objectMapper.readTree(json).path("Result").path("update");
        if (!update.isMissingNode()) timestamp = update.get("time").asText();
        Index index = new Index();
        index.setName(idx);
        index.setPrice(price);
        index.setTimestamp(timestamp);
        index.setAvgPrice(avgPrice);
        index.setIncrease(increase);
        index.setRatio(ratio);
        index.setAmount(amount);
        index.setTime(Index.patternTime(timestamp, SECOND_FORMAT));
        return parseHeader(json, idx, index);
    }

    @SneakyThrows
    public static List<Index> parseFiveDay(String json, String idx) {
        List<Index> res = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(json).path("Result").path("fivedays");
        if (dataNode == null || !dataNode.isArray()) return res;
        Iterator<JsonNode> days = dataNode.elements();
        while (days.hasNext()) {
            JsonNode day = days.next();
            String open = day.get("open") != null ? day.get("open").asText() : null;
            String preClose = day.get("preClose") != null ? day.get("preClose").asText() : null;
            if (day.get("priceinfos") == null || !day.get("priceinfos").isArray()) continue;
            Iterator<JsonNode> datas = day.get("priceinfos").elements();
            while (datas.hasNext()) {
                JsonNode data = datas.next();
                String timestamp = data.get("time") != null ? data.get("time").asText() : null;
                String price = data.get("price") != null ? data.get("price").asText() : null;
                String avgPrice = data.get("avgPrice") != null ? data.get("avgPrice").asText() : null;
                String amount = data.get("amount") != null ? data.get("amount").asText() : null;
                String ratio = data.get("ratio") != null ? data.get("ratio").asText() : null;
                String increase = data.get("increase") != null ? data.get("increase").asText() : null;
                res.add(Index.builder()
                        .timestamp(timestamp).price(price)
                        .avgPrice(avgPrice).amount(amount)
                        .ratio(ratio).increase(increase)
                        .open(open).preClose(preClose)
                        .name(idx).time(Index.patternTime(timestamp, MINUTE_FORMAT))
                        .build());
            }
        }
        return res;
    }


    @SneakyThrows
    public static Index parseHeader(String json, String name, Index index) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(json).path("Result").path("pankouinfos").path("list");
        if (dataNode.isMissingNode() || !dataNode.isArray()) return index;
        Iterator<JsonNode> elements = dataNode.elements();
        Map<String, String> map = new HashMap<>();
        while (elements.hasNext()) {
            JsonNode data = elements.next();
            map.put(data.get("ename").asText(), data.get("value").asText());
        }
        index.setOpen(map.get("open"));
        index.setHigh(map.get("high"));
        index.setPreClose(map.get("preClose"));
        index.setLow(map.get("low"));
        index.setAmplitudeRatio(map.get("amplitudeRatio"));
        index.setTotalAmount(map.get("amount"));
        index.setName(name);
        return index;
    }

    public static boolean isClose(String timestamp, Instant now) {
        Instant instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
        // 计算时间差
        Duration duration = Duration.between(instant, now);
        return Math.abs(duration.toMinutes()) <= 1;
    }
}
