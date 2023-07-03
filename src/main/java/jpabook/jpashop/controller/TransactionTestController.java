package jpabook.jpashop.controller;

import jpabook.jpashop.service.InnerTransactionService;
import jpabook.jpashop.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionTestController {

    private final TransactionService transactionService;
    private final InnerTransactionService innerTransactionService;

    @GetMapping("/api/tran")
    public void transactionTest() {
        transactionService.outerTransaction();
        innerTransactionService.outerTransaction();
    }
}
