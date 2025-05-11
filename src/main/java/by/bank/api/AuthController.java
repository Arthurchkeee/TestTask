package by.bank.api;

import by.bank.config.JwtUtil;
import by.bank.dto.LoginDto;
import by.bank.entity.User;
import by.bank.service.LoginService;
import by.bank.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final LoginService loginService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginRequest, HttpServletResponse response) {
        Optional<User> user = userService.findUserByEmailOrPhone(loginRequest.getEmail(), loginRequest.getPhone());
        if (user.isPresent() && loginService.isValidCredentials(loginRequest, user)) {
            String token = jwtUtil.generateToken(user.get().getId());
            ResponseCookie cookie = ResponseCookie.from("JWT", token).httpOnly(true).secure(true).path("/").maxAge(Duration.ofHours(3)).sameSite("Lax").build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            log.info("Login successful for user with ID {}", user.get().getId());
            return ResponseEntity.ok().build();
        }
        log.info("Login unsuccessfully for user with phone {} or email {}", loginRequest.getPhone(), loginRequest.getEmail());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}
