package com.filejournal.service;

public interface WeatherService {
    /**
     * 获取当前天气（根据 IP 定位城市）
     */
    String getCurrentWeather();

}
