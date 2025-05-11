package by.bank.api;

import by.bank.config.JwtFilter;
import by.bank.dto.AddEmailDto;
import by.bank.dto.AddPhoneDto;
import by.bank.dto.entityDto.EmailDataDto;
import by.bank.dto.entityDto.PhoneDataDto;
import by.bank.dto.TransferDto;
import by.bank.dto.entityDto.UserDto;
import by.bank.service.AccountService;
import by.bank.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AccountService accountService;
    private final JwtFilter jwtFilter;


    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(required = false) LocalDate dateOfBirth, @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) String name, @RequestParam(required = false) String email, @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return userService.searchUsers(dateOfBirth, phone, name, email, PageRequest.of(page, size));
    }

    @PostMapping("/phone")
    public UserDto addPhone(@Valid @RequestBody AddPhoneDto phone, HttpServletRequest httpRequest) {
        return userService.addPhone(phone.getPhone(), jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @PutMapping("/phone")
    public UserDto updatePhone(@Valid @RequestBody PhoneDataDto phoneDataDto, HttpServletRequest httpRequest) {
        return userService.updatePhone(phoneDataDto, jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @DeleteMapping("/phone")
    public UserDto deletePhone(@RequestBody Long phoneId, HttpServletRequest httpRequest) {
        return userService.deletePhone(phoneId, jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @PostMapping("/email")
    public UserDto addEmail(@Valid @RequestBody AddEmailDto email, HttpServletRequest httpRequest) {
        return userService.addEmail(email.getEmail(), jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @PutMapping("/email")
    public UserDto updateEmail(@Valid @RequestBody EmailDataDto emailDataDto, HttpServletRequest httpRequest) {
        return userService.updateEmail(emailDataDto, jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @DeleteMapping("/email")
    public UserDto deleteEmail(@RequestBody Long emailId, HttpServletRequest httpRequest) {
        return userService.deleteEmail(emailId, jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @PostMapping("/transfer")
    public void transfer(@Valid @RequestBody TransferDto transferDto, HttpServletRequest httpRequest) throws AccountNotFoundException {
        accountService.transfer(transferDto, jwtFilter.extractTokenFromCookie(httpRequest));
    }
}
