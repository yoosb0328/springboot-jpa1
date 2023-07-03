package jpabook.jpashop.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {
    private final EntityManager em;

    public void repoTransaction() {
        System.out.println("REPO : " + em.getDelegate());
    }
}
