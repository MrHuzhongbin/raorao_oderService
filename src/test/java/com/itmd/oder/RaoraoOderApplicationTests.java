package com.itmd.oder;

import com.itmd.oder.utils.PayHelper;
import com.itmd.oder.utils.PayState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RaoraoOderApplicationTests {
    @Autowired
    private PayHelper payHelper;
    @Test
    void contextLoads() {
//        String code = payHelper.createPayUrl(1234567890011222200L);
//        System.out.println(code);
        PayState payState = payHelper.queryOrder(1383420277043630000L);
        System.out.println(payState);
    }

}
