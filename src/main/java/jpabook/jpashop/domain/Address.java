package jpabook.jpashop.domain;

import lombok.Getter;
import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {
    private String city;

    private String street;

    private String zipcode;
    /*
    값 타입은 불변 객체로 설정해야 하므로 Setter를 모두 닫아 둔다.
    JPA 구현 라이브러리가 객체를 생성할 때 기본 생성자가 필요하므로 기본 생성자를 선언하되 안전을 위해 protected로 설정한다.
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
