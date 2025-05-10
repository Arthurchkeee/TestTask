package by.testtask.taskmanager.service;

import by.testtask.taskmanager.config.JwtUtil;
import by.testtask.taskmanager.dto.TransferDto;
import by.testtask.taskmanager.entity.Account;
import by.testtask.taskmanager.exception.InsufficientFundsException;
import by.testtask.taskmanager.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepo accountRepo;
    private final JwtUtil jwtUtil;
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.1");
    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("2.07");


    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    PessimisticLockingFailureException.class,
                    CannotAcquireLockException.class
            },
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(
            isolation = Isolation.SERIALIZABLE,
            timeout = 30
    )
    public void transfer(TransferDto transferDto, String token) throws AccountNotFoundException {
        Account accountFrom = accountRepo.findByUserId(Long.valueOf(jwtUtil.extractUserId(token))).orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
        Account accountTo = accountRepo.findByUserId(transferDto.getTransferTo()).orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        if (accountFrom.getBalance().compareTo(transferDto.getValue()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        } else {
            accountFrom.setBalance(accountFrom.getBalance().subtract(transferDto.getValue()));
            accountTo.setBalance(accountTo.getBalance().add(transferDto.getValue()));
            accountRepo.saveAll(List.of(accountFrom, accountTo));
        }

    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateBalances() {
        List<Account> accounts = accountRepo.findAll();
        for (Account account : accounts) {
            BigDecimal currentBalance = account.getBalance();
            BigDecimal newBalance = currentBalance.multiply(MULTIPLIER);
            BigDecimal maxBalance = account.getInitialBalance().multiply(MAX_PERCENTAGE);
            if (maxBalance.compareTo(currentBalance) > 0) {
                if (newBalance.compareTo(maxBalance) > 0) {
                    newBalance = maxBalance;
                }
                account.setBalance(newBalance);
                accountRepo.save(account);
            }

        }
    }
}
