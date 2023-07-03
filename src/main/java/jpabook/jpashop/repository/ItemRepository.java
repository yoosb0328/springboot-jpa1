package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if(item.getId() == null) {
            em.persist(item); //JPA에 저장하기 전까지는 id값이 없다. == 완전히 새로 생성한 객체. == save는 곧 insert
        } else {
            em.merge(item); // id값이 있다 == DB에 저장된 것을 가져온 객체 == save는 곧 update와 유사
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
