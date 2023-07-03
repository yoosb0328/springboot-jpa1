package jpabook.jpashop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InnerTransactionService {
    private final EntityManager em;

    @Transactional
    public void outerTransaction() {
        System.out.println("InnerTransactionService.Outer : " + em.getDelegate());
    }
}
