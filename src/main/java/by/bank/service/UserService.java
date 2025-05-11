package by.bank.service;

import by.bank.config.JwtUtil;
import by.bank.dto.entityDto.EmailDataDto;
import by.bank.dto.entityDto.PhoneDataDto;
import by.bank.dto.entityDto.UserDto;
import by.bank.entity.EmailData;
import by.bank.entity.PhoneData;
import by.bank.entity.User;
import by.bank.exception.AccessDeniedException;
import by.bank.exception.DataPersistingException;
import by.bank.exception.ResourceNotFoundException;
import by.bank.mapper.UserMapper;
import by.bank.repository.EmailDataRepository;
import by.bank.repository.PhoneDataRepository;
import by.bank.repository.UserRepository;
import by.bank.repository.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")

public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PhoneDataRepository phoneDataRepository;
    private final EmailDataRepository emailDataRepository;


    @Cacheable(key = "{#dateOfBirth, #phone, #name, #email, #pageable.pageNumber, #pageable.pageSize}",condition = "#pageable.pageSize > 10")
    public List<UserDto> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, Pageable pageable) {
        List<User> users = userRepository.findAll(UserSpecifications.buildUserSpecification(dateOfBirth, phone, name, email), pageable).toList();
        return userMapper.toDto(users);
    }
    @Caching(
            evict = {
                    @CacheEvict(allEntries = true, cacheNames = {"users", "userDetails"}),
                    @CacheEvict(key = "#result.id", cacheNames = "userDetails")
            }
    )
    public UserDto addPhone(String phone, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepository.findUserById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found when attempting to add phone", userId);
            return new ResourceNotFoundException("User not found");
        });

        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(phone);
        phoneData.setUser(user);

        try {
            phoneDataRepository.save(phoneData);
            user.getPhones().add(phoneData);
            log.info("Successfully added phone {} to user ID {}", phone, userId);
            return userMapper.toDto(user);
        } catch (Exception e) {
            log.error("Error adding phone {} to user ID {}: {}", phone, userId, e.getMessage());
            throw new DataPersistingException("Error saving phone", e);
        }
    }
    @Caching(
            evict = {
                    @CacheEvict(allEntries = true, cacheNames = {"users", "userDetails"}),
                    @CacheEvict(key = "#result.id", cacheNames = "userDetails")
            }
    )
    public UserDto updatePhone(PhoneDataDto phoneDataDto, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepository.findUserById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found when attempting to update phone", userId);
            return new ResourceNotFoundException("User not found");
        });

        PhoneData phoneData = phoneDataRepository.findById(phoneDataDto.getId()).orElseThrow(() -> {
            log.error("Phone with ID {} not found when attempting to update", phoneDataDto.getId());
            return new ResourceNotFoundException("Phone not found");
        });

        if (!phoneData.getUser().equals(user)) {
            log.error("Phone with ID {} does not belong to user ID {} during update attempt", phoneDataDto.getId(), userId);
            throw new AccessDeniedException("Access denied: cannot modify this phone");
        }

        phoneData.setPhone(phoneDataDto.getPhone());
        try {
            phoneDataRepository.save(phoneData);
            log.info("Successfully updated phone ID {} for user ID {} to {}", phoneDataDto.getId(), userId, phoneDataDto.getPhone());
            return userMapper.toDto(user);
        } catch (Exception e) {
            log.error("Error updating phone ID {} for user ID {}: {}", phoneDataDto.getId(), userId, e.getMessage());
            throw new DataPersistingException("Error updating phone", e);
        }
    }
    @Caching(
            evict = {
                    @CacheEvict(allEntries = true, cacheNames = {"users", "userDetails"}),
                    @CacheEvict(key = "#result.id", cacheNames = "userDetails")
            }
    )
    public UserDto deletePhone(Long phoneId, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepository.findUserById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found when attempting to delete phone", userId);
            return new ResourceNotFoundException("User not found");
        });

        PhoneData phoneData = phoneDataRepository.findById(phoneId).orElseThrow(() -> {
            log.error("Phone with ID {} not found when attempting to delete", phoneId);
            return new ResourceNotFoundException("Phone not found");
        });

        if (!phoneData.getUser().equals(user)) {
            log.error("Phone with ID {} does not belong to user ID {} during deletion attempt", phoneId, userId);
            throw new AccessDeniedException("Access denied: cannot delete this phone");
        }

        try {
            phoneDataRepository.delete(phoneData);
            user.getPhones().remove(phoneData);
            log.info("Successfully deleted phone ID {} from user ID {}", phoneId, userId);
            return userMapper.toDto(user);
        } catch (Exception e) {
            log.error("Error deleting phone ID {} from user ID {}: {}", phoneId, userId, e.getMessage());
            throw new DataPersistingException("Error deleting phone", e);
        }
    }
    @Caching(
            evict = {
                    @CacheEvict(allEntries = true, cacheNames = {"users", "userDetails"}),
                    @CacheEvict(key = "#result.id", cacheNames = "userDetails")
            }
    )
    public UserDto addEmail(String email, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepository.findUserById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found when attempting to add email", userId);
            return new ResourceNotFoundException("User not found");
        });

        EmailData emailData = new EmailData();
        emailData.setEmail(email);
        emailData.setUser(user);

        try {
            emailDataRepository.save(emailData);
            user.getEmails().add(emailData);
            log.info("Successfully added email {} to user ID {}", email, userId);
            return userMapper.toDto(user);
        } catch (Exception e) {
            log.error("Error adding email {} to user ID {}: {}", email, userId, e.getMessage());
            throw new DataPersistingException("Error saving email", e);
        }
    }
    @Caching(
            evict = {
                    @CacheEvict(allEntries = true, cacheNames = {"users", "userDetails"}),
                    @CacheEvict(key = "#result.id", cacheNames = "userDetails")
            }
    )
    public UserDto updateEmail(EmailDataDto emailDataDto, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepository.findUserById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found when attempting to update email", userId);
            return new ResourceNotFoundException("User not found");
        });

        EmailData emailData = emailDataRepository.findById(emailDataDto.getId()).orElseThrow(() -> {
            log.error("Email with ID {} not found when attempting to update", emailDataDto.getId());
            return new ResourceNotFoundException("Email not found");
        });

        if (!emailData.getUser().equals(user)) {
            log.error("Email with ID {} does not belong to user ID {} during update attempt", emailDataDto.getId(), userId);
            throw new AccessDeniedException("Access denied: cannot modify this email");
        }

        emailData.setEmail(emailDataDto.getEmail());
        try {
            emailDataRepository.save(emailData);
            log.info("Successfully updated email ID {} for user ID {} to {}", emailDataDto.getId(), userId, emailDataDto.getEmail());
            return userMapper.toDto(user);
        } catch (Exception e) {
            log.error("Error updating email ID {} for user ID {}: {}", emailDataDto.getId(), userId, e.getMessage());
            throw new DataPersistingException("Error updating email", e);
        }
    }
    @Caching(
            evict = {
                    @CacheEvict(allEntries = true, cacheNames = {"users", "userDetails"}),
                    @CacheEvict(key = "#result.id", cacheNames = "userDetails")
            }
    )
    public UserDto deleteEmail(Long emailId, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepository.findUserById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found when attempting to delete email", userId);
            return new ResourceNotFoundException("User not found");
        });

        EmailData emailData = emailDataRepository.findById(emailId).orElseThrow(() -> {
            log.error("Email with ID {} not found when attempting to delete", emailId);
            return new ResourceNotFoundException("Email not found");
        });

        if (!emailData.getUser().equals(user)) {
            log.error("Email with ID {} does not belong to user ID {} during deletion attempt", emailId, userId);
            throw new AccessDeniedException("Access denied: cannot delete this email");
        }

        try {
            emailDataRepository.delete(emailData);
            user.getEmails().remove(emailData);
            log.info("Successfully deleted email ID {} from user ID {}", emailId, userId);
            return userMapper.toDto(user);
        } catch (Exception e) {
            log.error("Error deleting email ID {} from user ID {}: {}", emailId, userId, e.getMessage());
            throw new DataPersistingException("Error deleting email", e);
        }
    }
    public Optional<User> findUserByEmailOrPhone(String email, String phone) {
        if (StringUtils.hasText(email)) {
            return emailDataRepository.findByEmail(email).map(EmailData::getUser);
        }
        if (StringUtils.hasText(phone)) {
            return phoneDataRepository.findByPhone(phone).map(PhoneData::getUser);
        }
        return Optional.empty();
    }
}
