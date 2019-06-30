package com.dawnwin.stick;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * create by jordan on 2019/6/30
 */
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.dawnwin.stick.mapper")
public class DeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);
    }
}




