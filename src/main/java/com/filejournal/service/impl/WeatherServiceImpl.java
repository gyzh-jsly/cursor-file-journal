package com.filejournal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filejournal.service.WeatherService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 和风天气 API（免费版，需要注册获取 key）
    private static final String API_KEY = "你的和风天气API密钥";  // 暂时用占位符，后续可配置

    @Override
    public String getCurrentWeather() {
        try {
            // 1. 先获取 IP 定位城市
            String city = getCityByIp();
            if (city == null) {
                return "⛅ 未知城市";
            }

            // 2. 获取天气
            String url = "https://devapi.qweather.com/v7/weather/now?location=" + city + "&key=" + API_KEY;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            String weather = root.path("now").path("text").asText();
            String temp = root.path("now").path("temp").asText();

            return getWeatherEmoji(weather) + " " + weather + " " + temp + "°C";
        } catch (Exception e) {
            // 网络异常时返回默认值
            return "⛅ 天气获取失败";
        }
    }

    /**
     * 通过 IP 获取城市名
     */
    private String getCityByIp() {
        try {
            String url = "http://ip-api.com/json/";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("city").asText();
        } catch (Exception e) {
            return null;
        }
    }

    private String getWeatherEmoji(String weather) {
        if (weather.contains("晴")) return "☀️";
        if (weather.contains("云")) return "⛅";
        if (weather.contains("阴")) return "☁️";
        if (weather.contains("雨")) return "🌧️";
        if (weather.contains("雪")) return "❄️";
        return "🌡️";
    }
}