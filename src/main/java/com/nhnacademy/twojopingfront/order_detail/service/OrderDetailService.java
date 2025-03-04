package com.nhnacademy.twojopingfront.order_detail.service;


import com.nhnacademy.twojopingfront.order_detail.client.OrderDetailClient;
import com.nhnacademy.twojopingfront.order_detail.dto.response.OrderDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderDetailService {
    private final OrderDetailClient orderDetailClient;

    public OrderDetailResponseDto getOrderDetail(Long orderId){
        return orderDetailClient.getOrderDetail(orderId);
    }
    public Page<OrderDetailResponseDto> getOrderDetailsByCustomerId(int page, int size) {
        return orderDetailClient.getOrderDetailsByCustomerId(page,size);
    }
}
