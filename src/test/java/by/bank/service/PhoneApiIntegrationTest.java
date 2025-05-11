package by.bank.service;

import by.bank.config.JwtUtil;
import by.bank.dto.AddPhoneDto;
import by.bank.dto.entityDto.PhoneDataDto;
import by.bank.entity.PhoneData;
import by.bank.entity.User;
import by.bank.repository.PhoneDataRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PhoneApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhoneDataRepository phoneDataRepository;
    @Autowired
    private JwtUtil jwtUtil;

    private User user;
    private String authToken;
    private PhoneData existingPhone;

    @BeforeEach
    void setUp() {
        user = createTestUser("testuser");
        authToken = jwtUtil.generateToken(user.getId());
        existingPhone = createTestPhone(user, "+375291234567");
    }

    @AfterEach
    void tearDown() {
        phoneDataRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addPhone_ValidRequest_ReturnsUpdatedUser() throws Exception {
        AddPhoneDto newPhone = new AddPhoneDto("+375441112233");

        assertThat(phoneDataRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/phone")
                        .cookie(new Cookie("JWT", authToken)) // 1. Исправляем имя куки
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPhone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones.length()").value(2));

        List<PhoneData> phones = phoneDataRepository.findAll();
        assertThat(phones).hasSize(2);
        assertThat(phones.get(1).getPhone()).isEqualTo(newPhone.getPhone());
    }

    @Test
    void updatePhone_ValidRequest_UpdatesPhone() throws Exception {
        PhoneDataDto updateDto = new PhoneDataDto(existingPhone.getId(), "+375332223344");

        mockMvc.perform(put("/phone")
                        .cookie(new Cookie("JWT", authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones[0].phone").value(updateDto.getPhone()));

        PhoneData updated = phoneDataRepository.findById(existingPhone.getId()).get();
        assertThat(updated.getPhone()).isEqualTo(updateDto.getPhone());
    }

    @Test
    void deletePhone_ValidRequest_RemovesPhone() throws Exception {
        mockMvc.perform(delete("/phone")
                        .cookie(new Cookie("JWT", authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingPhone.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones").isEmpty());

        assertThat(phoneDataRepository.existsById(existingPhone.getId())).isFalse();
    }

    @Test
    void updatePhone_OtherUsersPhone_ThrowsAccessDenied() throws Exception {
        User otherUser = createTestUser("otheruser");
        PhoneData otherPhone = createTestPhone(otherUser, "+375297654321");
        PhoneDataDto updateDto = new PhoneDataDto(otherPhone.getId(), "+375000000000");

        mockMvc.perform(put("/phone")
                        .cookie(new Cookie("JWT", authToken)) // 1. Исправлено имя куки на "jwt"
                        .contentType(MediaType.APPLICATION_JSON) // 2. Добавлен Content-Type
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setName(username);
        user.setPassword("password");
        return userRepository.save(user);
    }

    private PhoneData createTestPhone(User user, String number) {
        PhoneData phone = new PhoneData();
        phone.setPhone(number);
        phone.setUser(user);
        return phoneDataRepository.save(phone);
    }
}
