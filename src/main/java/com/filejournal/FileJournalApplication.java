package com.filejournal;

import javax.annotation.PostConstruct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
@MapperScan("com.filejournal.mapper")
public class FileJournalApplication {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(FileJournalApplication.class, args);
    }
    

    @PostConstruct
    public void testRedis() {
        redisTemplate.opsForValue().set("test", "Hello Redis");
        String value = redisTemplate.opsForValue().get("test");
        System.out.println("Redis 连接测试: " + value);
    }
}
