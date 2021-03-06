package com.itmd.oder.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WXPayConfiguration {

    @Bean
    public WXPay wxPay(PayConfig payConfig){
        return new WXPay(payConfig);
    }
}
