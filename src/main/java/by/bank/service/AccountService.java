package by.bank.service;

import by.bank.config.JwtUtil;
import by.bank.dto.TransferDto;
import by.bank.entity.Account;
import by.bank.exception.DataPersistingException;
import by.bank.exception.InsufficientFundsException;
import by.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.1");
    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("2.07");


    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class, CannotAcquireLockException.class}, maxAttempts = 5, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 30)
    public void transfer(TransferDto transferDto, String token) throws AccountNotFoundException {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        Account accountFrom = accountRepository.findByUserId(userId).orElseThrow(() -> {
            log.error("Sender account not found for user ID: {}", userId);
            return new AccountNotFoundException("Sender account not found");
        });
        Account accountTo = accountRepository.findByUserId(transferDto.getTransferTo()).orElseThrow(() -> {
            log.error("Receiver account not found for user ID: {}", transferDto.getTransferTo());
            return new AccountNotFoundException("Receiver account not found");
        });
        if (accountFrom.getBalance().compareTo(transferDto.getValue()) < 0) {
            log.warn("Insufficient funds for transfer: User {} balance {} < transfer amount {}", userId, accountFrom.getBalance(), transferDto.getValue());
            throw new InsufficientFundsException("Insufficient funds");
        }
        try {
            accountFrom.setBalance(accountFrom.getBalance().subtract(transferDto.getValue()));
            accountTo.setBalance(accountTo.getBalance().add(transferDto.getValue()));
            accountRepository.saveAll(List.of(accountFrom, accountTo));

            log.info("Transfer successful. New balances - From: {}, To: {}", accountFrom.getBalance(), accountTo.getBalance());
        } catch (Exception e) {
            log.error("Error processing transfer from {} to {}: {}", userId, transferDto.getTransferTo(), e.getMessage());
            throw new DataPersistingException("Error processing transfer", e);
        }

    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateBalances() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            try {
                BigDecimal currentBalance = account.getBalance();
                BigDecimal newBalance = currentBalance.multiply(MULTIPLIER);
                BigDecimal maxBalance = account.getInitialBalance().multiply(MAX_PERCENTAGE);

                if (maxBalance.compareTo(currentBalance) > 0) {
                    if (newBalance.compareTo(maxBalance) > 0) {
                        newBalance = maxBalance;
                    }
                    account.setBalance(newBalance);
                    accountRepository.save(account);
                    log.trace("Updated balance for account {}: {} -> {}", account.getId(), currentBalance, newBalance);
                }
            } catch (Exception e) {
                log.error("Error updating balance for account {}: {}", account.getId(), e.getMessage());
                throw new DataPersistingException("Error updating balance", e);
            }

        }
    }
}
