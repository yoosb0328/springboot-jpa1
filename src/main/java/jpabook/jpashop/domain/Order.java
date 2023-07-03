package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 조인컬럼 FK이름 지정
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL )
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    /*
    연관관계 편의 메서드
     */
    public void setMember(Member member){
        //Member와 Order가 양방향 연관관계인데, Order의 member를 설정하면 Member의 orders에도 member를 추가해줌.
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this) ;
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this );
    }

    /*
    생성 메서드
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems){
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    /*
    비즈니스 로직
    비즈니스 로직이 대부분 엔티티에 있어 서비스 계층에서 단순히 엔티티에 필요한 요청을 위임하는 패턴 : 도메인 모델 패턴
    반대로 서비스 계층에서 대부분의 로직을 처리하는 패턴 : 트랜잭션 스크립트 패턴
     */
    /**
     * 주문 취소
     */
     public void cancle() {
         if(delivery.getStatus() == DeliveryStatus.COMP){
             throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다");
         }
         this.setStatus(OrderStatus.CANCEL);
         for(OrderItem orderItem : orderItems) {
             orderItem.cancle();
         }
     }

     /*
     조회 로직
      */
    /**
     * 전체 주문 가격
     */
    public int getTotalPrice() {
//        int totalPrice = 0;
//        for(OrderItem orderItem : orderItems) {
//            totalPrice += orderItem.getTotalPrice();
//        }
//        return totalPrice;
        return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}
