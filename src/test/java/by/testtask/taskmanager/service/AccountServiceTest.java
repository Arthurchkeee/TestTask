package by.testtask.taskmanager.service;

import by.testtask.taskmanager.config.JwtUtil;
import by.testtask.taskmanager.dto.TransferDto;
import by.testtask.taskmanager.entity.Account;
import by.testtask.taskmanager.entity.EmailData;
import by.testtask.taskmanager.entity.PhoneData;
import by.testtask.taskmanager.entity.User;
import by.testtask.taskmanager.repo.AccountRepo;
import by.testtask.taskmanager.repo.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AccountServiceTest {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtUtil jwtUtil;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private User user1;
    private User user2;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("test1");
        user1.setDateOfBirth(LocalDate.now());
        user1.setPassword("123456781");

        user2 = new User();
        user2.setName("test2");
        user2.setDateOfBirth(LocalDate.now());
        user2.setPassword("123456782");

        userRepo.saveAll(List.of(user1, user2));

        account1 = new Account();
        account1.setUser(user1);
        account1.setBalance(BigDecimal.valueOf(1000));
        account1.setInitialBalance(BigDecimal.valueOf(1000));

        account2 = new Account();
        account2.setUser(user2);
        account2.setBalance(BigDecimal.valueOf(2000));
        account2.setInitialBalance(BigDecimal.valueOf(2000));

        accountRepo.saveAll(List.of(account1, account2));
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        accountRepo.deleteAll();
    }

    @Test
    void whenTransferAndUpdateBalanceConcurrently_thenNoDataInconsistency()
            throws InterruptedException, AccountNotFoundException {

        // Получаем актуальные ID
        Long receiverId = user2.getId();

        // Запуск updateBalances в отдельном потоке
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> accountService.updateBalances());

        // Параллельный вызов transfer()
        TransferDto dto = new TransferDto(receiverId, BigDecimal.valueOf(500));
        accountService.transfer(dto, jwtUtil.generateToken(user1.getId()));

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        // Обновляем данные из БД
        Account updatedAccount1 = accountRepo.findById(account1.getId()).orElseThrow();
        Account updatedAccount2 = accountRepo.findById(account2.getId()).orElseThrow();

        assertThat(updatedAccount1.getBalance())
                .isBetween(BigDecimal.valueOf(500), BigDecimal.valueOf(1500));
        assertThat(updatedAccount2.getBalance())
                .isBetween(BigDecimal.valueOf(2500), BigDecimal.valueOf(3000));
    }

    @Test
    void whenOptimisticLockExceptionOccurs_thenRetrySucceeds() {
        // Используем динамический ID
        Account account = accountRepo.findById(account1.getId()).orElseThrow();
        account.setBalance(BigDecimal.valueOf(1000));
        accountRepo.save(account);

        Executors.newSingleThreadExecutor().submit(() -> {
            Account parallelAccount = accountRepo.findById(account1.getId()).orElseThrow();
            parallelAccount.setBalance(BigDecimal.valueOf(1500));
            accountRepo.save(parallelAccount);
        });

        TransferDto dto = new TransferDto(user2.getId(), BigDecimal.valueOf(500));
        assertThatCode(() -> accountService.transfer(dto, jwtUtil.generateToken(user1.getId())))
                .doesNotThrowAnyException();
    }


    @Test
    void whenSerializableIsolation_thenPreventPhantomReads() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setIsolationLevel(Isolation.SERIALIZABLE.value());


        Long targetAccountId = account1.getId();

        txTemplate.execute(status -> {
            try {
                Account account = accountRepo.findById(targetAccountId)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

                BigDecimal initialBalance = account.getBalance();

                Executors.newSingleThreadExecutor().submit(() -> {
                    Account parallelAccount = accountRepo.findById(targetAccountId).orElseThrow();
                    parallelAccount.setBalance(parallelAccount.getBalance().add(BigDecimal.valueOf(500)));
                    accountRepo.save(parallelAccount);
                });


                Thread.sleep(1000);

                Account refreshedAccount = accountRepo.findById(targetAccountId).orElseThrow();
                assertThat(refreshedAccount.getBalance()).isEqualTo(initialBalance);

            } catch (InterruptedException | AccountNotFoundException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}