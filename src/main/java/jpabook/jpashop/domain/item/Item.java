package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //싱글 테이블 전략 선택
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    /*
    ===== 비즈니스 로직 =====
    정보(로직에서 사용할 필드)를 가지고 있는 엔티티에 비즈니스 로직을 설계하는 것이 응집성이 높다.
    데이터를 수정할 일이 있다면 Setter가 아닌 비즈니스 로직 메서드를 통해 접근하는 것이 객체지향적 (Setter는 닫아놓는 것 권장)
     */

    /**
     * Stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }
    /**
     * Stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException ("need more stock");
        }
        this.stockQuantity = restStock;
    }

}
