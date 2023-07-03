package jpabook.jpashop.service;

import jpabook.jpashop.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final EntityManager em;
    private final InnerTransactionService innerTransactionService;
    private final TransactionRepository transactionRepository;
    @Transactional
    public void outerTransaction() {
        System.out.println("TransactionService.Outer : " + em.getDelegate());
        innerTransactionService.outerTransaction();
        transactionRepository.repoTransaction();
        innerTransaction();
    }

    @Transactional
    public void innerTransaction() {
        System.out.println("TransactionService.Inner : " + em.getDelegate());
    }
}
