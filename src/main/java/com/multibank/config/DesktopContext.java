package com.multibank.config;

import com.multibank.integration.BcrBankClient;
import com.multibank.integration.BtBankClient;
import com.multibank.integration.BankApiClient;
import com.multibank.repository.BankAccountRepository;
import com.multibank.repository.SavingsPlanRepository;
import com.multibank.repository.TransactionRepository;
import com.multibank.service.*;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**

 * Wires JPA (Hibernate), repositories, integration clients, and services.
 */
public class DesktopContext {

    private final EntityManagerFactory emf;

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsPlanRepository savingsPlanRepository;

    private final List<BankApiClient> bankClients;

    private final BankAccountService bankAccountService;
    private final TransactionService transactionService;
    private final SavingsPlanService savingsPlanService;
    private final BankSynchronizationService bankSynchronizationService;
    private final AnalyticsService analyticsService;
    private final QrCodeService qrCodeService;

    public DesktopContext() {
        // JPA setup
        Properties jpaProps = new Properties();
        // Defaults mirror application.yml previous Spring config
        jpaProps.setProperty("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        jpaProps.setProperty("jakarta.persistence.jdbc.url", "jdbc:h2:mem:multibank;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        jpaProps.setProperty("jakarta.persistence.jdbc.user", "sa");
        jpaProps.setProperty("jakarta.persistence.jdbc.password", "password");
        jpaProps.setProperty("hibernate.hbm2ddl.auto", "update");
        jpaProps.setProperty("hibernate.format_sql", "true");
        jpaProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        this.emf = Persistence.createEntityManagerFactory("multibankPU", jpaProps);

        // Repositories
        this.bankAccountRepository = new BankAccountRepository(emf);
        this.transactionRepository = new TransactionRepository(emf);
        this.savingsPlanRepository = new SavingsPlanRepository(emf);

        // Integration clients (stub/offline by default)
        BankIntegrationProperties integrationProps = new BankIntegrationProperties();
        this.bankClients = new ArrayList<>();
        this.bankClients.add(new BcrBankClient(integrationProps));
        this.bankClients.add(new BtBankClient(integrationProps));

        // Services
        this.bankAccountService = new BankAccountService(bankAccountRepository);
        this.transactionService = new TransactionService(transactionRepository);
        this.savingsPlanService = new SavingsPlanService(savingsPlanRepository);
        this.bankSynchronizationService = new BankSynchronizationService(bankClients, bankAccountRepository, transactionRepository);
        this.analyticsService = new AnalyticsService(transactionRepository);
        this.qrCodeService = new QrCodeService();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public BankAccountService getBankAccountService() { return bankAccountService; }
    public TransactionService getTransactionService() { return transactionService; }
    public SavingsPlanService getSavingsPlanService() { return savingsPlanService; }
    public BankSynchronizationService getBankSynchronizationService() { return bankSynchronizationService; }
    public AnalyticsService getAnalyticsService() { return analyticsService; }
    public QrCodeService getQrCodeService() { return qrCodeService; }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}

