package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import com.shop.repository.item.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;


    public Long order(OrderDto orderDto, String email){
        //itemid로 item 찾아옴. email로 member 찾아옴
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);

        //OrderItemList 생성. orderitem 생성.item 재고 감소. orderitemlist에 orderitem 추가
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item,orderDto.getCount());
        orderItemList.add(orderItem);

        //member, orderitemlist 넘겨서 order 생성, 저장
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);


        return order.getId();
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable){
        //주문목록, 주문수 조회
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        //OrderHistDto 에 주문목록 추가
        //order 엔티티 -> dto
        //orderitem 엔티티 -> dto
        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for(Order order : orders){
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            //item id, 대표이미지여부 로 itemimg 찾아와서
            //orderitem 엔티티 , img url 로 orderitemdto 생성
            //order dto 에 orderitemdto 추가
            for(OrderItem orderItem : orderItems){
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }
        //
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {
        //email로 member, orderid 로 order 찾기
        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        //order에 연관되어 있는 member 불러오기
        Member savedMember = order.getMember();
        //member 랑 order.member 다르면 return false
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;
        }
        return true;
    }

    public void cancelOrder(Long orderId) {
        //order id 로 order 찾기
        //order 상태 cancel 로 바꾸고. orderitem 들어가서 item 재고수 증가
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();
    }

    public Long orders(List<OrderDto> orderDtoList, String email) {
        //email로 member 조회
        //orderitemlist 생성
        //orderdtolist에서 orderdto 하나씩 꺼냄
        //orderdto(itemid, count)의 id 로 item 조회

        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList  = new ArrayList<>();
        for(OrderDto orderDto : orderDtoList){
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);
            //OrderItem 엔티티 생성, item과 연관관계 설정, count, orderprice set
            //item 재고 감소
            //(dto-> 엔티티. item 재고 감소)
            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }
        //member, orderItemList 넘겨서 order 생성
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();

    }
}
