package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 특정 엔티티를 찾는 것이 아니라
 * 특정 화면이나 API에 fit하게 개발하는 쿼리들을 모음.
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); //query 1번 -> 결과 N개 count(Order)

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); //query N번  :: 1+N 문제
            o.setOrderItems(orderItems);
        });

        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, oi.item.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                                "from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        /*
        V5
         */
        List<OrderQueryDto> result = findOrders(); //쿼리 1번
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        //쿼리 2번  --> 총 쿼리 2번으로 최적화
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {

        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, oi.item.name, oi.orderPrice, oi.count) " +
                                "from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        //Map에 저장하는 이유 -> 메모리에 올려놓고 orderItems를 채우고자 (쿼리 안날라가도록)
        Map<Long, List<OrderItemQueryDto>> orderItemMap =  orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        /*
        V6
        조회해야할 테이블을 모두 조인하여 1번의 쿼리로 데이터를 가져오는 방법.
        flat 데이터로 만들었으므로 중복 데이터가 생긴다 (=> 조회는 편리하겠지만 반정규화를 수행한 것과 같음)
        중복데이터가 생기므로 데이터 크기가 커져 상황에 따라서는 속도가 느릴 수 있다.
        flat 데이터를 그대로 화면에 뿌리는 경우는 많지 않으므로 대부분 애플리케이션에서 추가 분해 작업 해야..(화면의 필요에 맞게)
        페이징 불가능
         */
        return em.createQuery(
                "select new" +
                        " jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
