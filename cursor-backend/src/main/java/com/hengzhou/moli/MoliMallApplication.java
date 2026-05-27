package com.hengzhou.moli;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 横州茉莉花电商系统启动类
 *
 * @author 横州茉莉花
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@MapperScan("com.hengzhou.moli.mapper")
public class MoliMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoliMallApplication.class, args);
    }
}
