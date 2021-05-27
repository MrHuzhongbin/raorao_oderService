package com.itmd.oder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "raorao.worker")
@Data
public class IdWorkerProperties {
    private Long workerId;
    private Long dataCenterId;
}
