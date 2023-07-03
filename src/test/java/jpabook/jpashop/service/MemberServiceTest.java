package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;
/*
jUnit 4 사용
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    /*
    **** SpringBootTest에서(Service나 Repository에서 말고) ****
    Transactional은 커밋이 아닌 롤백을 한다 => 엔티티매니저가 플러시를 하지 않아 쿼리가 날라가지 않는다.
    쿼리가 날라가는걸 확인하고 싶다면 롤백을 꺼주거나 엔티티매니저를 강제로 플러쉬한다.
     */
    @Test
//    @Rollback(false)
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("Kim");
        //when
        Long saveId = memberService.join(member);
        //then
        em.flush();//커밋을 시켜서 쿼리를 보기 위해서
        assertEquals(member, memberRepository.findById(saveId).get());
        /*
        JPA 내에서 같은 트랜잭션안에 있다면, id가 같다면 같은 객체임(영속성 컨테이너에 등록되어 있으니까)
         */
    }
    
    @Test(expected = IllegalStateException.class) //IllegalStateException이 발생하면 성공처리
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2); //중복일 경우 예외 발생해야 함.

        //then
        fail("예외가 발생해야 합니다."); //여기로 떨어지면 테스트 실패..중복을 못잡은것
    }
}