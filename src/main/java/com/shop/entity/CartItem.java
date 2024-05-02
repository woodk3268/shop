package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity@Getter@Setter
@Table(name="cart_item")
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cart_item_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="item_id")
    private Item item;

    private int count;

    public static CartItem createCartItem(Cart cart, Item item, int count){
        //Cartitem 생성. cart. item 의존관계 설정. count set
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(count);

        return cartItem;
    }
    public void addCount(int count){
        this.count+=count;
    }

    public void updateCount(int count){
        this.count=count;
    }
}
