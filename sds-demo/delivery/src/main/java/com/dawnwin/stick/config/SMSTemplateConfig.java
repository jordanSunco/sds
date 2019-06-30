package com.dawnwin.stick.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "sms.aliyun")
@ComponentScan
public class SMSTemplateConfig {
    private String accessKey;
    private String secretKey;
    private String templateCode;
}