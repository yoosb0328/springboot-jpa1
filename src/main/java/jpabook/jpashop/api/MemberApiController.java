package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController // => @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMember = memberService.findMembers();
        List<MemberDto> collect = findMember.stream()
                    .map(m -> new MemberDto(m.getName()))
                    .collect(Collectors.toList());
//        return new Result(collect.size(), collect);
        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        //List를 바로 return하면 JSON 배열로 바로 나가버리기 때문에(=>유연성 떨어짐) Result 클래스로 감싸준다.
//        private int count; //유연하게 데이터 추가해서 넘기기 가능.
        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        //@RequestBody : JSON 데이터를 Member로 매핑해줌.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
        //장점 : DTO 클래스를 안만들어도 된다. 단점 : 장점을 제외한 모든 ...
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
        /*
        별도의 DTO 클래스를 사용함으로써 api 스펙을 변경안해도 됨.
        DTO 클래스를 통해 api 스펙을 알기 쉽다.
        엔티티를 변경해도 api 스펙을 변경하지 않을 수 있다.
        엔티티가 외부에 노출되지 않는다.
         */
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }



    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
