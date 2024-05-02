package com.shop.service;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.*;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.item.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email){
        //cartItemDto 에서 itemid 꺼내서 그 id로 item 조회
        //email로 member 조회
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member= memberRepository.findByEmail(email);

        //member의 아이디로 cart 조회
        //cart가 없으면 만들어서 repository에 저장
        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart==null){
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }
        //cartid, itemid 로 cartItem 찾아옴
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());
        if(savedCartItem != null){
            //찾아온 cartItem 있으면 수량 증가
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        }else{
            //찾아온 cartItem 없으면 cart,item, 수량 넘겨서 cartItem 생성, 저장
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email){
        //cartDetailDtoList 생성
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        //email 로 member, cart 찾아옴
        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        //cart가 null이면 빈 객체 반환
        if(cart==null){
            return cartDetailDtoList;
        }
        //cart가 있으면, cartId 로 cartitemdetail 찾아옴
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;

    }
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email){
        //email로 member, cartItemId로 cartItem 조회
        //cartItem에서 cart 꺼내서 member 조회
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember();

        //꺼낸 member 랑 현재 member email 다르면 return false
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;
        }
        return true;
    }
    public void updateCartItemCount(Long cartItemId, int count){
        //cartItemId로 cartitem 조회 , cartItem 수량 update
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItem.updateCount(count);
    }
    public void deleteCartItem(Long cartItemId){
        //cartItemId로 cartitem 조회 , cartItemRepository에서 cartItem삭제
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {
        //orderDtoList 생성
        //CartItemId 담긴 CartOrderDTO 하나씩 꺼내서 repository에서 cartItem 찾아옴
        //OrderDto 생성. itemid, count set
        //orderDtoList, email 넘겨서 주문완료
        List<OrderDto> orderDtoList = new ArrayList<>();
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }
        Long orderId = orderService.orders(orderDtoList, email);

        //CartItemId 담긴 CartOrderDTO 하나씩 꺼내서 repository에서 cartItem 찾아옴
        //repository에서 delete
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }
}
