package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.repository.order.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * x To One 에서의 성능 최적화
 * Order
 * Order -> Member  (M to O)
 * Order -> Delivery (O to O)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        /*
        JSON 생성 과정에서 Order에서 Member로, 다시 Member에서 Order로 가면서 무한 루프에 빠진다...
        참고(스프링은 Jackson 라이브러리 사용)
        따라서 양방향 연관관계라면 한쪽에 @JsonIgnore를 해줘야함.
        그러나 @JsonIgnore를 사용한다고 문제가 끝나는 것은 아님.
        지연 로딩 전략에 따라 프록시 역할을 하는 객체(ByteBuddyInterceptor)가 Member 객체 대신 들어와있는데
        Jackson 라이브러리가 해당 객체를 파싱하지 못함.
        해결책 : Hibernate5 모듈의 FORCE LAZY LOADING 사용. -> 프록시 부분도 모두 강제로 가져오게 됨. -> 쿼리 엄청 많이 실행됨...
        그러나 애초에 이러한 방식을 사용안하는게 좋음!
        필요한 데이터만 가져올 수 있도록 DTO 객체를 활용하자.
         */
        return all;
    }
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch()); //ORDER 2개 조회
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(result);
        /*
        ORDER -> SQL 1번 실행 -> 결과 2개(ORDER 2개) -> 각 ORDER 별로 LAZY 초기화(DTO 참고) 시 SQL 2번 실행 -> 총 SQL이 5번이나 실행됨... 1+N+N 실행...
        결론 : 성능 개선 필요!
              => 페치 조인을 사용하자.
         */
    }

    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(); //fetch 조인으로 쿼리 1회만 실행.완
        //엔티티를 DTO로 변환 (엔티티를 외부로 노출하지 말자)
        List<SimpleOrderDto> result = orders.stream() // DTO 변환하는 과정에서 지연로딩으로 인한 프록시 초기화 실행
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(result);
    }

    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4() {
        return new Result(orderSimpleQueryRepository.findOrderDtos());
    }
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화 : 영속성 컨텍스트에 값이 없다 => DB에서 찾아온다. (MEMBER 테이블에 SELECT)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화 : 영속성 컨텍스트에 값이 없다 => DB에서 찾아온다.(DELIVERY 테이블에 SELECT)
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
