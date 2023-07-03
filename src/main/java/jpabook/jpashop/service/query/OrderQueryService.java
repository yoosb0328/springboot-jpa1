package jpabook.jpashop.service.query;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OSIV를 false로 설정함으로써
 * 지연로딩이 발생하는 로직을 트랜잭션 안으로 위치시켜야 한다.
 * 이 경우 핵심 로직이 아니므로 쿼리용 서비스로 분리한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;
    public List<OrderDto> orderV3_OSIV() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }


}
