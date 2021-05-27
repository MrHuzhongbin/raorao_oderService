package com.itmd.oder.service;

import com.itmd.enums.ExceptionEnum;
import com.itmd.exception.RaoraoBookShopException;
import com.itmd.oder.client.BookClient;
import com.itmd.oder.client.UserClient;
import com.itmd.oder.enums.OrderStatusEnum;
import com.itmd.oder.mapper.OrderDetailMapper;
import com.itmd.oder.mapper.OrderMapper;
import com.itmd.oder.mapper.OrderStatusMapper;
import com.itmd.oder.pojo.Order;
import com.itmd.oder.pojo.OrderDetail;
import com.itmd.oder.pojo.OrderStatus;
import com.itmd.oder.utils.IdWorker;
import com.itmd.oder.vi.OrderData;
import com.itmd.oder.vi.OrderIO;
import com.itmd.pojo.Book;
import com.itmd.pojo.UserAddress;
import com.itmd.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private UserClient userClient;
    @Autowired
    private BookClient bookClient;

    @Transactional
    public Long creatOrder(OrderIO orderIO) {
        Order order = new Order();
        order.setCreateTime(new Date());
        //生成订单Id
        long id = idWorker.nextId();
        order.setOrderId(id);
        //获取用户
//        UserInfo user = LoginInterceptor.getUser();
        order.setUserId(2L);
        order.setBuyerNick("杨雪");
        order.setBuyerMessage("");
        order.setBuyerRate(false);
        //获取支付类型
        order.setPaymentType(orderIO.getPaymentType());
        //获取收货人信息
        UserAddress userAddress = null;
        try {
            userAddress = userClient.queryAddressById(orderIO.getAddrId());
            order.setReceiver(userAddress.getReceiver());
            order.setReceiverMobile(userAddress.getPhone());
            order.setReceiverState(userAddress.getProvince());
            order.setReceiverCity(userAddress.getCity());
            order.setReceiverDistrict(userAddress.getDistrict());
            order.setReceiverAddress(userAddress.getAddress());
        } catch (Exception e) {
           log.info("获取收货人失败！【失败原因】：未找到收货人或者权限不足！");
           return null;
        }
        //获取图书信息
        Map<Long, Integer> map = orderIO.getOrderData().stream().
                collect(Collectors.toMap(OrderData::getId, OrderData::getNum));
        Set<Long> ids = map.keySet();
        List<Book> books = null;
        try {
           books = bookClient.queryBookByIds(new ArrayList<>(ids));
        } catch (Exception e) {
            log.info("获取图书信息失败！【失败原因】：未找到图书或者权限不足！");
            return null;
        }
        //获取总价格

        float money = 0;
        List<OrderDetail>list = new ArrayList<>();
        for (Book book : books) {
            money+=book.getPrice()*map.get(book.getId());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            orderDetail.setBId(book.getId());
            orderDetail.setTitle(book.getInfo());
            orderDetail.setNum(map.get(book.getId()));
            orderDetail.setImage(StringUtils.split(book.getImage(),",")[0]);
            orderDetail.setPrice(book.getPrice());
            list.add(orderDetail);
        }
        order.setTotalPay(money);
        //邮费
        order.setPostFee(0f);
        //实付金额=总价格+邮费-优惠
        order.setActualPay(money+0-0);
        int stats = orderMapper.insertSelective(order);
        if(stats!=1){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_CREATE_ERROR);
        }
        //创建OrderDetail
        stats = orderDetailMapper.insertList(list);
        if(stats<1){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_CREATE_ERROR);
        }
        //创建OrderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(id);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.getCode());
        stats = orderStatusMapper.insertSelective(orderStatus);
        if(stats!=1){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_CREATE_ERROR);
        }
        //减掉库存
        List<OrderData> orderData = orderIO.getOrderData();
        bookClient.reduceStock(orderData);

        return id;
    }

    public Order queryOrderById(Long id) {

        Order order = orderMapper.selectByPrimaryKey(id);
        if(order == null){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_FOUND_ERROR);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(orderDetails)){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_FOUND_ERROR);
        }
        order.setOrderDetails(orderDetails);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if(orderStatus == null ){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_FOUND_ERROR);
        }
        order.setStatus(orderStatus.getStatus());
        return order;
    }

    public PageResult<Order> queryOrderByPage(int page, int rows, String key, int status) {
        return null;
    }

    @Transactional
    public void updateOrderStatus(Long id, int status) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(id);
        orderStatus.setStatus(status);
        switch (status){
            case 2:
                orderStatus.setPaymentTime(new Date());
                break;
            case 3:
                orderStatus.setConsignTime(new Date());
                break;
            case 4:
                orderStatus.setEndTime(new Date());
                break;
            case 5:
                orderStatus.setCloseTime(new Date());
                break;
            case 6:
                orderStatus.setCommentTime(new Date());
                break;
            default:
                throw new RaoraoBookShopException(ExceptionEnum.ORDER_UPDATE_STATUS_ERROR);
        }
        int tag = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if(tag!=1){
            throw new RaoraoBookShopException(ExceptionEnum.ORDER_UPDATE_STATUS_ERROR);
        }
    }
}
