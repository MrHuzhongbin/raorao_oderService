package com.itmd.oder.web;

import com.itmd.oder.pojo.Order;
import com.itmd.oder.service.OrderService;
import com.itmd.oder.utils.PayHelper;
import com.itmd.oder.utils.PayState;
import com.itmd.oder.vi.OrderIO;
import com.itmd.enums.ExceptionEnum;
import com.itmd.exception.RaoraoBookShopException;
import com.itmd.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PayHelper payHelper;
    /**
     * 创建订单
     * @param orderIO
     * @return
     */
    @PostMapping("OrderNumber")
    public ResponseEntity<Long> creatOrder(@RequestBody OrderIO orderIO){
        Long id = orderService.creatOrder(orderIO);
        if(id == null){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_CREATE_ERROR);
        }
        return ResponseEntity.ok(id);
    }

    /**
     * 查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order>queryOrderById(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 分页查询订单
     * @param page
     * @param rows
     * @param key
     * @param status
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Order>>queryOrderByPage(@RequestParam(value = "page",defaultValue = "1")int page,
                                                             @RequestParam(value = "rows",defaultValue = "10")int rows,
                                                             @RequestParam(value = "key",defaultValue = "null")String key,
                                                             @RequestParam(value = "status",required = false)int status
    ){
        return ResponseEntity.ok(orderService.queryOrderByPage(page,rows,key,status));
    }

    /**
     * 更新订单状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("{id}/{status}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable("id")Long id,@PathVariable("status")int status){
        orderService.updateOrderStatus(id,status);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 获取支付链接
     * @param id
     * @return
     */
    @GetMapping("pay/{id}")
    public ResponseEntity<String> getOrderPayUrl(@PathVariable("id")Long id){
        String payUrl = payHelper.createPayUrl(id);
        if(payUrl == null){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_CREATE_ERROR);
        }
        return ResponseEntity.ok(payUrl);
    }

    /**
     * 获取支付状态
     * @param id
     * @return
     */
    @GetMapping("pay/status")
    public ResponseEntity<Integer>queryPayStatus(@RequestParam("id")Long id){
        PayState payState = payHelper.queryOrder(id);
        return ResponseEntity.ok(payState.getValue());
    }
}
