package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());

        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        //굳이 Member엔티티가 아닌 MemberForm 객체를 따로 만들어서 쓰는 이유
        // -> 1. 화면에 딱 맞는 데이터를 만들어서 사용하기 위해서 2. 엔티티가 지저분해지는 것 방지.(화면과 응집도가 높아져서 지저분해짐) -> 유지보수 어려움!
        // 엔티티는 외부로 노출해서는 안됨.
        //@Valid  : 객체의 Validation 어노테이션을 검증한다.
        //BindingResult : 오류가 발생 시 멈추지 않고 오류를 담아서 코드를 실행시킬 수 있다.
        /*
        createMemberForm에서 이름 입력 안하고 submit하면
         */
        if(result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
