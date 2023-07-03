package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.repository.order.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count){
        //엔티티 조회
        Member member = memberRepository.findById(memberId).get();
        Item item = itemRepository.findOne(itemId);
        //배송정보 등록
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        //주문상품 생성
        /*
        생성 시 createOrderItem 메서드를 구현해서 생성할 수 있지만 누군가는 생성자로 객체를 생성 후
        setter로 값을 채울 수도 있다. 생성 방식이 중구난방되는 것을 방지하고자 생성자를 protected로 설정한다.
         */
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장(Order 엔티티 살펴보면 orderItem 필드와 delivery 필드에 cascade.ALL 옵션있으므로 order만 저장해도 orderItem, delivery 저장됨)
        orderRepository.save(order);

        return order.getId();
    }
    /**
     * 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancle();
    }
    /**
     * 검색
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }


}
