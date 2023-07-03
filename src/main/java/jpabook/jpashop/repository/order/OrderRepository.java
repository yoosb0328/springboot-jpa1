package jpabook.jpashop.repository.order;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;

@Repository
public class OrderRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;
    /*
    JPAQueryFactory를 클래스 내에서 static으로 쓰기 위해서 @RequiredArgsConstructor 대신 직접 생성자 구현하여 사용.
    em이 먼저 생성되고 JPAQueryFactory에 매개변수로 넘겨줘야 하므로..
     */
    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    /*
        status나 name에 해당하는 값이 없다면 ? 쿼리가 변해야 함... -> 동적 쿼리 필요!
        1. 조건문을 이용해서 query string을 수동으로 조립한다.
        2. JPA 표준 Criteria를 사용한다
        -> 둘다 실무에서 사용하기에는 너무 복잡하고 유지보수성이 떨어진다.
        3. Querydsl을 사용한다.
    */
//    public List<Order> findAll(OrderSearch orderSearch) {
//        return em.createQuery("select o from Order o join o.member m" +
//                        " where o.status = :status " +
//                        " and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .setMaxResults(1000) //최대 1000건
//                .getResultList();
//
//    }
    /**
     * JPA Criteria
     * 유지 보수성이 너무 떨어져서 실무에서 사용하지는 않음
     * Querydsl로 변경할 것.
     */
    public List<Order> findAll_Criteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);

        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /**
     * Querydsl 방식 동적 쿼리
     * 직접 String으로 쿼리를 작성하는 것과 비교하면 오타로 인한 에러 발생 가능성이 현저히 낮다.(컴파일러가 다잡아주니까!)
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String memberName) {
        if(!StringUtils.hasText(memberName)) return null;
        return member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) return null;
        return order.status.eq(statusCond);
    }
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList(); //LAZY 무시하고 한번에 3개 테이블 조인해서 프록시 없이 실제 값을 채워서 반환함.
        /*/
        심플 쿼리 방식(DTO 직접 조회 방식)보다 재사용성이 높음
         */
    }


    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" + //distinct : 쿼리에 distinct 붙임 + 엔티티가 중복인 경우 중복을 걸러서 컬렉션에 담아줌
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithMemberDeliveryPage(int offset, int limit) {
        /*
        application.yml에 default_batch_fetch_size 적용하여서 where절에 in 조건 적용한 쿼리가 실행됨
        ToOne 관계는 그대로 fetch 조인하면 되고 ToMany관계는 따로 쿼리에 적지 않아도 알아서 in 조건으로 찾아줌.
        (사실 select o from Order o 까지만 해도 ToOne관계도 in 조건으로 찾아주지만 ToOne 관계는 fetch 조인으로 하나의 조인 쿼리로 처리해도
        페이징에 영향을 주지 않으므로 fetch 해서 조회하자)
         */
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList(); //LAZY 무시하고 한번에 3개 테이블 조인해서 프록시 없이 실제 값을 채워서 반환함.
        /*/
        심플 쿼리 방식(DTO 직접 조회 방식)보다 재사용성이 높음
         */
    }
}
