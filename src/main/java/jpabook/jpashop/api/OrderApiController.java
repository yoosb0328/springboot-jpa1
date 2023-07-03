package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.repository.order.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 컬렉션 조회 최적화
 *  일대다 관계 = 컬렉션
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderQueryService orderQueryService;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        /*
        엔티티 직접 노출 : 사용하지 말자.
         */
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for(Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().map(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        /*
        엔티티 직접 노출은 방지하였으나.
        지연 로딩으로 인해 너무 많은 SQL이 실행된다.
         */
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }
    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    /**
     * OSIV FALSE 옵션에 따른
     * 지연로딩 프록시 초기화 로직 분리.
     */
    @GetMapping("/api/v3.OSIV/orders")
    public Result ordersV3_OSIV() {
        return new Result(orderQueryService.orderV3_OSIV());
    }

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     *-ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화 */
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDeliveryPage(offset, limit);

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return new Result(collect);
    }
    @GetMapping("/api/v4/orders")
    public Result orderV4() {
        List<OrderQueryDto> collect = orderQueryRepository.findOrderQueryDtos();
        return new Result(collect);
    }

    @GetMapping("/api/v5/orders")
    public Result orderV5() {
        List<OrderQueryDto> collect = orderQueryRepository.findAllByDto_optimization();
        return new Result(collect);
    }

    @GetMapping("/api/v6/orders")
    public Result orderV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return new Result(flats);
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
//            orderItems = order.getOrderItems(); //
            /*
             orderItem은 Order와 양방향, 지연로딩인 엔티티 -> 프록시가 넘어옴 -> 초기화 필요
            DTO안에 엔티티가 있으므로 엔티티가 노출되는 상황임. -> orderItem조차도 DTO로 바꿔야 한다.
             */
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }
    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
    @Data
    @AllArgsConstructor
    static class Result<T> {
        //List를 바로 return하면 JSON 배열로 바로 나가버리기 때문에(=>유연성 떨어짐) Result 클래스로 감싸준다.
//        private int count; //유연하게 데이터 추가해서 넘기기 가능.
        private T data;
    }

}
