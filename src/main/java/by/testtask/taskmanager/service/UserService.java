package by.testtask.taskmanager.service;

import by.testtask.taskmanager.config.JwtUtil;
import by.testtask.taskmanager.dto.entityDto.EmailDataDto;
import by.testtask.taskmanager.dto.entityDto.PhoneDataDto;
import by.testtask.taskmanager.dto.entityDto.UserDto;
import by.testtask.taskmanager.entity.EmailData;
import by.testtask.taskmanager.entity.PhoneData;
import by.testtask.taskmanager.entity.User;
import by.testtask.taskmanager.exception.AccessDeniedException;
import by.testtask.taskmanager.exception.ResourceNotFoundException;
import by.testtask.taskmanager.mapper.UserMapper;
import by.testtask.taskmanager.repo.EmailDataRepo;
import by.testtask.taskmanager.repo.PhoneDataRepo;
import by.testtask.taskmanager.repo.UserRepo;
import by.testtask.taskmanager.repo.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PhoneDataRepo phoneDataRepo;
    private final EmailDataRepo emailDataRepo;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /*@Cacheable(value = "usersCache", key = "{#dateOfBirth, #phone, #name, #email, #pageable}")*/
    public List<UserDto> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, Pageable pageable) {
        List<User> users = userRepo.findAll(UserSpecifications.buildUserSpecification(dateOfBirth, phone, name, email), pageable).toList();
        return userMapper.toDto(users);
    }

    public UserDto addPhone(String phone, String token) {
        MDC.put("add_phone", phone);
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepo.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(phone);
        phoneData.setUser(user);
        phoneDataRepo.save(phoneData);
        user.getPhones().add(phoneData);
        return userMapper.toDto(user);
    }

    public UserDto updatePhone(PhoneDataDto phoneDataDto, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepo.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PhoneData phoneData = phoneDataRepo.findById(phoneDataDto.getId()).orElseThrow(() -> new ResourceNotFoundException("Phone not found"));

        if (!phoneData.getUser().equals(user)) {
            throw new AccessDeniedException("Access denied: cannot modify this phone");
        }

        phoneData.setPhone(phoneDataDto.getPhone());
        phoneDataRepo.save(phoneData);

        return userMapper.toDto(user);
    }

    public UserDto deletePhone(Long phoneId, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepo.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PhoneData phoneData = phoneDataRepo.findById(phoneId).orElseThrow(() -> new ResourceNotFoundException("Phone not found"));

        if (!phoneData.getUser().equals(user)) {
            throw new AccessDeniedException("Access denied: cannot delete this phone");
        }

        phoneDataRepo.delete(phoneData);
        user.getPhones().remove(phoneData);
        return userMapper.toDto(user);
    }

    public UserDto addEmail(String email, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepo.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmailData emailData = new EmailData();
        emailData.setEmail(email);
        emailData.setUser(user);
        emailDataRepo.save(emailData);
        user.getEmails().add(emailData);
        return userMapper.toDto(user);
    }

    public UserDto updateEmail(EmailDataDto emailDataDto, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepo.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmailData emailData = emailDataRepo.findById(emailDataDto.getId()).orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        if (!emailData.getUser().equals(user)) {
            throw new AccessDeniedException("Access denied: cannot modify this email");
        }

        emailData.setEmail(emailDataDto.getEmail());
        emailDataRepo.save(emailData);

        return userMapper.toDto(user);
    }

    public UserDto deleteEmail(Long emailId, String token) {
        Long userId = Long.valueOf(jwtUtil.extractUserId(token));
        User user = userRepo.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmailData emailData = emailDataRepo.findById(emailId).orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        if (!emailData.getUser().equals(user)) {
            throw new AccessDeniedException("Access denied: cannot delete this email");
        }

        emailDataRepo.delete(emailData);
        user.getEmails().remove(emailData);
        return userMapper.toDto(user);
    }
    public Optional<User> findUserByEmailOrPhone(String email, String phone) {
        if (StringUtils.hasText(email)) {
            return emailDataRepo.findByEmail(email).map(EmailData::getUser);
        }
        if (StringUtils.hasText(phone)) {
            return phoneDataRepo.findByPhone(phone).map(PhoneData::getUser);
        }
        return Optional.empty();
    }
}
