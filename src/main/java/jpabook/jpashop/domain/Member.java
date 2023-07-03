package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @GeneratedValue @Id
    @Column(name = "member_id") //@Column이 없으면 변수명을 그대로 컬럼명으로 쓴다.
    private Long id;
    @NotEmpty
    private String name;
    @Embedded
    private Address address;
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();


}
