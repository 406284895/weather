package com.yipan.weather.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @description:
 * @author: gaocuifang
 * @create: 2021-09-12
 **/
@Component
public class WeatherClient {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Lazy
    private WeatherClient weatherClient;

    public String provinces() {
        return weatherClient.get(UrlConstant.PROVINCE_URL);
    }

    public String citysByProvince(String provinceCode) {
        return weatherClient.get(String.format(UrlConstant.CITY_URL, provinceCode));
    }

    public String countrysByCity(String provinceCode, String cityCode) {
        return weatherClient.get(String.format(UrlConstant.COUNTRY_URL, provinceCode, cityCode));
    }

    public String weatherByCountry(String provinceCode, String cityCode, String countryCode) {
        return weatherClient.get(String.format(UrlConstant.WEATHER_URL, provinceCode, cityCode, countryCode));
    }

    /**
     * get 请求
     *
     * @description: 使用spring 的 retry 失败时重新尝试调用3次
     */
    @Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000L, multiplier = 2))
    public String get(String url) {
        System.out.println("request url: " + url);
        return restTemplate.getForEntity(url, String.class).getBody();
    }
}
