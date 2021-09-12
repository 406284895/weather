package com.yipan.weather.demo;

/**
 * @description:
 * @author: gaocuifang
 * @create: 2021-09-12
 **/
public interface UrlConstant {
    /**
     * Get the province code of China
     */
    String PROVINCE_URL = "http://www.weather.com.cn/data/city3jdata/china.html";

    /**
     * Get the city code of one certain province
     */
    String CITY_URL = "http://www.weather.com.cn/data/city3jdata/provshi/%s.html";

    /**
     * Get the county code of one certain city
     */
    String COUNTRY_URL = "http://www.weather.com.cn/data/city3jdata/station/%s%s.html";

    /**
     * Get the weather of one certain county
     */
    String WEATHER_URL = "http://www.weather.com.cn/data/sk/%s%s%S.html";
}
