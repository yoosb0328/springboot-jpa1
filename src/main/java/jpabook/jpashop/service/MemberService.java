package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //읽기 메서드가 많으므로 전체는 readOnly로 설정.
@RequiredArgsConstructor //final 필드를 가지고 생성자를 만들어줌
public class MemberService {
    //repository는 바꿀 일이 없으므로 final로 선언하는 것 추천. 생성자 미구현시 컴파일러가 체크도 해줌
    private final MemberRepository memberRepository;

    /*
    Setter Injection 방식 : 필드에 다른 repository를 주입할 수 있으므로 테스트 시 용이.
    하지만 테스트 이외 상황에 바꿀 일이 없고 바뀔 경우 에러발생할 수 있음.. app실행시 조립이 끝나니까
     */
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
    /*
    생성자 injection
    중간에 setter로 repository를 바꿀 수 없음(안전).
    생성자가 하나 뿐이면 어노테이션 없어도 자동으로 인젝션 해준다 .
     */
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    //회원가입
    @Transactional //읽기전용아니므로 개별적으로 설정(default는 readOnly = false)
    public Long join(Member member){
        validateDuplicationMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicationMember(Member member){
        /*
        실제 상황이라면 동시에 같은 name의 member를 등록한다면 검증로직을 통과할 수 있으므로
        name에 중복을 허용하지 않으려면 DB에 Unique제약 조건을 걸어주는 것이 확실한 방법.
        spring-data-jpa : findByName은 JpaRepository에 기본적으로 구현되어 있지 않으므로 직접 구현한다.
         */
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        //Exception
    }

    //회원전체조회
//    @Transactional(readOnly = true) //조회하는 경우에는 읽기전용 트랜잭션 설정을 하면 성능 부하를 줄일 수 있다.
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
//    @Transactional(readOnly = true)
    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).get();
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findById(id).get();
        member.setName(name);
    }
}
