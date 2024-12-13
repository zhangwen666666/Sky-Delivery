package com.sky.config;

import com.sky.properties.HwObsProperties;
import com.sky.utils.HwObsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建HwObsUtil对象
 */
@Configuration
@Slf4j
public class ObsConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HwObsUtil hwObsUtil(HwObsProperties hwObsProperties){
        log.info("开始创建华为云文件上传工具类对象：{}", hwObsProperties);
        return new HwObsUtil(hwObsProperties.getEndpoint(), hwObsProperties.getAccessKeyId(),
                hwObsProperties.getAccessKeySecret(), hwObsProperties.getBucketName());
    }
}
