package com.multibank.controller;

import com.multibank.dto.BankAccountResponse;
import com.multibank.service.BankAccountService;
import com.multibank.service.BankSynchronizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
public class BankIntegrationController {

    private final BankSynchronizationService synchronizationService;
    private final BankAccountService bankAccountService;

    public BankIntegrationController(BankSynchronizationService synchronizationService,
                                     BankAccountService bankAccountService) {
        this.synchronizationService = synchronizationService;
        this.bankAccountService = bankAccountService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> synchronize() {
        synchronizationService.synchronizeAll();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponse>> getAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }
}
