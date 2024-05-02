package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;
import com.shop.repository.item.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    MemberRepository memberRepository;
    @PersistenceContext
    EntityManager em;

    public Item createItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }
    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest(){
        //Order 엔티티 생성
        Order order = new Order();

        for(int i=0;i<3;i++){
            //item 생성, repository에 저장
            //OrderItem 생성, item,order 연관관계 설정
            //order에 orderItem set
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }
        //order 저장
        orderRepository.saveAndFlush(order);
        em.clear();

        //order id 로 repository에서 찾아옴.
        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);
        //order의 orderitem 저장되었는지 확인
        //(orderitem을 직접적으로 저장한적은 없음)
        assertEquals(3,savedOrder.getOrderItems().size());
    }
    public Order createOrder(){
        //Order 엔티티 생성
        Order order = new Order();

        for(int i=0;i<3;i++){
            //item 생성, 저장
            //OrderItem 생성, item,order 연관관계 설정
            //order에 orderItem set
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }
        //member 생성, 저장
        Member member = new Member();
        memberRepository.save(member);

        //order에 member 의존관계 설정
        //order 저장
        order.setMember(member);
        orderRepository.save(order);
        return order;

    }
    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        //order 엔티티 생성
        //order - orderitem 연관관계 끊음--> orderitem 삭제 쿼리
        Order order = this.createOrder();
        order.getOrderItems().remove(0);
        em.flush();
    }
    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){
        //Order 엔티티 생성
        //0번째 orderitem orderitemid 얻어옴
        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        //orderitem Repository에서 orderitemid로 orderitem 찾아옴
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);
        //order 클래스 조회
        System.out.println("Order class : "+ orderItem.getOrder().getClass());
        System.out.println("=================");
        orderItem.getOrder().getOrderDate();
        System.out.println("=================");


    }


}