package com.itmd.oder.vi;

import lombok.Data;

import java.util.List;

@Data
public class OrderIO {

    private Long addrId;
    private int paymentType;
    private List<OrderData> orderData;

}
