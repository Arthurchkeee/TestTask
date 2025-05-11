package by.bank.service;

import by.bank.config.JwtUtil;
import by.bank.dto.TransferDto;
import by.bank.entity.Account;
import by.bank.entity.User;
import by.bank.repository.AccountRepository;
import by.bank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired AccountService accountService;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

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
    private String authToken;
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

        userRepository.saveAll(List.of(user1, user2));

        account1 = new Account();
        account1.setUser(user1);
        account1.setBalance(BigDecimal.valueOf(1000));
        account1.setInitialBalance(BigDecimal.valueOf(1000));

        account2 = new Account();
        account2.setUser(user2);
        account2.setBalance(BigDecimal.valueOf(2000));
        account2.setInitialBalance(BigDecimal.valueOf(2000));

        accountRepository.saveAll(List.of(account1, account2));
        authToken = jwtUtil.generateToken(user1.getId());

    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void whenTransferAndUpdateBalanceConcurrently_thenNoDataInconsistency() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.execute(status -> {
                accountService.updateBalances(); // Непосредственный вызов метода сервиса
                return null;
            });
        });

        TransferDto transferDto = new TransferDto(user2.getId(), BigDecimal.valueOf(500));
        mockMvc.perform(post("/transfer")
                        .cookie(new Cookie("JWT", authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk());

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        Account updatedFrom = accountRepository.findById(account1.getId()).orElseThrow();
        Account updatedTo = accountRepository.findById(account2.getId()).orElseThrow();

        assertThat(updatedFrom.getBalance())
                .isBetween(BigDecimal.valueOf(500), BigDecimal.valueOf(1500));
        assertThat(updatedTo.getBalance())
                .isBetween(BigDecimal.valueOf(2500), BigDecimal.valueOf(3000));
    }

    @Test
    void whenOptimisticLockExceptionOccurs_thenRetrySucceeds() throws Exception {
        account1.setBalance(BigDecimal.valueOf(1000));
        accountRepository.save(account1);

        Executors.newSingleThreadExecutor().submit(() -> {
            Account parallelAccount = accountRepository.findById(account1.getId()).orElseThrow();
            parallelAccount.setBalance(BigDecimal.valueOf(1500));
            accountRepository.save(parallelAccount);
        });

        TransferDto transferDto = new TransferDto(user2.getId(), BigDecimal.valueOf(500));
        mockMvc.perform(post("/transfer")
                        .cookie(new Cookie("JWT", authToken)) // Изменено здесь
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk());

        Account result = accountRepository.findById(account1.getId()).orElseThrow();
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void whenSerializableIsolation_thenPreventPhantomReads() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setIsolationLevelName("ISOLATION_SERIALIZABLE");

        Long targetAccountId = account1.getId();

        txTemplate.execute(status -> {
            try {
                Account account = accountRepository.findById(targetAccountId)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

                BigDecimal initialBalance = account.getBalance();

                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        mockMvc.perform(post("/transfer")
                                        .cookie(new Cookie("JWT", authToken)) // Изменено здесь
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                new TransferDto(user2.getId(), BigDecimal.valueOf(500))
                                        )))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                Thread.sleep(1000);

                Account refreshedAccount = accountRepository.findById(targetAccountId).orElseThrow();
                assertThat(refreshedAccount.getBalance()).isEqualTo(initialBalance);

            } catch (InterruptedException | AccountNotFoundException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }


}
