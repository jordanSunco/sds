package com.dawnwin.app.stick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * create by lorne on 2017/10/13
 */

@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
public class StickSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(StickSocketApplication.class, args);
    }
}