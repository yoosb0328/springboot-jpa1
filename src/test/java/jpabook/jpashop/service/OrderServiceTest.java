package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.order.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("Harry Potter", 10, 10000);

        int beforeCount = book.getStockQuantity();
        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", beforeCount - orderCount, book.getStockQuantity());
    }

    private Item createBook(String name, int stockQuantity, int price) {
//        Item book = new Book(name, price, stockQuantity);
        Item book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantity);
        book.setPrice(price);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "XX길", "123-1234"));
        em.persist(member);
        return member;
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("Harry Potter", 10, 10000);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);//주문한 상황.

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("주문 취소시 상태는 CANCEL이다.", OrderStatus.CANCEL, getOrder.getStatus());
        //cancle()메서드 내에 addStock() 메서드도 있음
        assertEquals("주문이 취소된 상품은 재고가 원상복구되어야 한다.", 10, book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("Harry Potter", 10, 10000);

        int orderCount = 11; //stockQuantity 초과함
        //when
        orderService.order(member.getId(), book.getId(), orderCount);
        //then
        fail("재고 수량 예외가 발생하여 fail까지 내려오면 안됨");
    }
}