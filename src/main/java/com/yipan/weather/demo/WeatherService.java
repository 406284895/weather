package com.yipan.weather.demo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WeatherService {
    @Autowired
    private WeatherClient weatherClient;

    @Autowired
    private Gson gson;
    private ThreadPoolExecutor executor =
            new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), Executors
                    .defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


    private static LoadingCache<Long, AtomicLong> guava = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, AtomicLong>() {
                @Override
                public AtomicLong load(Long seconds) throws Exception {
                    return new AtomicLong(0);
                }
            });
    public static long permit = 100;

    public Optional<Integer> getTemperature(String province, String city, String county) {
        long currentSeconds = System.currentTimeMillis() / 1000;
        try {
            if (guava.get(currentSeconds).incrementAndGet() > permit) {
                return Optional.empty();
            }
        } catch (ExecutionException e) {
            System.out.println(e.getMessage());
        }
        CountDownLatch downLatch = new CountDownLatch(1);
        FutureTask<Optional<Integer>> task = new FutureTask(new Callable<Optional<Integer>>() {
            @Override
            public Optional<Integer> call() throws Exception {
                return getTemperature(province, city, county, downLatch);
            }
        });
        executor.submit(task);
        try {
            downLatch.await(100, TimeUnit.SECONDS);
            if (task.isDone()) {
                return task.get();
            } else {
                task.cancel(true);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (ExecutionException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Integer> getTemperature(String province, String city, String county, CountDownLatch countDownLatch) {
        JsonObject weatherJson = null;
        try {
            String provinceStr = weatherClient.provinces();
            if (!StringUtils.hasLength(provinceStr)) {
                return Optional.empty();
            }
            Map<String, String> provinceMap = gson.fromJson(provinceStr, HashMap.class);
            String provinceCode =
                    provinceMap.entrySet().stream().filter(entry -> entry.getValue().equals(province)).findFirst()
                            .orElseThrow(() -> new Exception("province is not correct")).getKey();
            String cityStr = weatherClient.citysByProvince(provinceCode);
            if (!StringUtils.hasLength(cityStr)) {
                return Optional.empty();
            }
            Map<String, String> cityMap = gson.fromJson(cityStr, HashMap.class);
            String cityCode =
                    cityMap.entrySet().stream().filter(entry -> entry.getValue().equals(city)).findFirst()
                            .orElseThrow(() -> new Exception("city is not correct")).getKey();
            String countryStr = weatherClient.countrysByCity(provinceCode, cityCode);
            if (!StringUtils.hasLength(countryStr)) {
                return Optional.empty();
            }
            Map<String, String> countryMap = gson.fromJson(countryStr, HashMap.class);
            String countryCode =
                    countryMap.entrySet().stream().filter(entry -> entry.getValue().equals(county)).findFirst()
                            .orElseThrow(() -> new Exception("country is not correct")).getKey();
            String weatherStr = weatherClient.weatherByCountry(provinceCode, cityCode, countryCode);
            weatherJson = JsonParser.parseString(weatherStr).getAsJsonObject();
            return weatherJson.has("weatherinfo") ?
                    Optional.of(Double.valueOf(weatherJson.getAsJsonObject("weatherinfo").get("temp").getAsString())
                            .intValue()) :
                    Optional.empty();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            countDownLatch.countDown();
        }
        return Optional.empty();
    }
}
