package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {
    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;
    @NotEmpty(message = "도시를 입력해주세요.")
    private String city;
    private String street;
    private String zipcode;
}
