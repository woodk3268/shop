package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto,
                                              BindingResult bindingResult, Principal principal){
            //error 있으면 body에 message 를 담음
            if(bindingResult.hasErrors()){
                StringBuilder sb = new StringBuilder();
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                for(FieldError fieldError : fieldErrors){
                    sb.append(fieldError.getDefaultMessage());
                }
                return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
            }

            String email  = principal.getName();
            Long cartItemId;
            //email, cartitemdto 넘겨서 cart에 추가
            try{
                cartItemId = cartService.addCart(cartItemDto, email);
            }catch (Exception e){
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);

            }
            return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
    @GetMapping("/cart")
    public String orderHist(Principal principal, Model model){
        //email로 cart 정보 조회
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
        model.addAttribute("cartItems", cartDetailList);
        return "cart/cartList";

    }
    @PatchMapping("/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(
            @PathVariable("cartItemId") Long cartItemId, int count, Principal principal
    ){
        //count가 0보다 작거나 같으면 bad request
        //cartitemid 넘겨서 cart에 담은 회원, 지금 회원 같은지 조회
        //같으면 수량 update
        if(count<=0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요",HttpStatus.BAD_REQUEST);
        }else if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);

    }
    @DeleteMapping("/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(
            @PathVariable("cartItemId") Long cartItemId, Principal principal
    ){
       if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);

    }
    @PostMapping("/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(
            @RequestBody CartOrderDto cartOrderDto, Principal principal
            ){
        //CartOrderDTOList 꺼냄
        //List에 든 CartOrderDto 없으면 forbidden 응답

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        if(cartOrderDtoList == null || cartOrderDtoList.size() ==0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }
        //CartItem 하나씩 다 꺼내서 repository에서 cartItem 찾아옴. cart에 담은 member 와 지금 member 일치하는지 검사
        for(CartOrderDto cartOrder : cartOrderDtoList){
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);

    }


}
