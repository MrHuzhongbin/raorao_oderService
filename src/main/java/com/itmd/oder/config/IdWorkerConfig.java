package com.itmd.oder.config;

import com.itmd.oder.utils.IdWorker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(IdWorkerProperties.class)
@Configuration
public class IdWorkerConfig {

    @Bean
    public IdWorker getIdWorker(IdWorkerProperties prop){
        return new IdWorker(prop.getWorkerId(),prop.getDataCenterId());
    }
}
