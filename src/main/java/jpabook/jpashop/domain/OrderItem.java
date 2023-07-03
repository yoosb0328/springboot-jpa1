package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //다른곳에서 NoArgs생성자 호출 방지.
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID")
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;
    private int count;

    /*
        생성 메서드
         */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice); //item.price가 아닌 orderPrice를 따로 넣는 이유 (=할인 등으로 주문시 가격이 변할 수 있으니까!)
        orderItem.setCount(count);

        item.removeStock(count); //주문을 했다 = 재고가 줄어든다!
        return orderItem;
    }
    /*
    비즈니스 로직
     */
    public void cancle() {
        getItem().addStock(count);
    }
    /*
    조회 로직
     */
    /**
     * 주문상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
