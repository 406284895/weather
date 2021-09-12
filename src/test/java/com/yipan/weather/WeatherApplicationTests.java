package com.yipan.weather;

import com.yipan.weather.demo.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;


@SpringBootTest(classes = WeatherApplication.class)
@RunWith(SpringRunner.class)
class WeatherApplicationTests {
    @Autowired
    private WeatherService weatherService;

    @Test
    public void success() {
        System.out.println(weatherService.getTemperature("江苏", "苏州", "张家港").get());
    }

    @Test
    public void fail() {
        System.out.println(weatherService.getTemperature("江苏", "苏州", "南阳").isPresent());
    }

    @Test
    public void batch() {
        for (int i = 0; i <= 200; i++) {
            Optional result = weatherService.getTemperature("江苏", "苏州", "张家港");
            System.out.println("第【" + i + "】次查询【张家港】温度：" + (result.isPresent() ? result.get() : "失败"));
        }
    }
}
