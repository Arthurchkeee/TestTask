package by.testtask.taskmanager.api;

import by.testtask.taskmanager.config.JwtFilter;
import by.testtask.taskmanager.dto.entityDto.EmailDataDto;
import by.testtask.taskmanager.dto.entityDto.PhoneDataDto;
import by.testtask.taskmanager.dto.TransferDto;
import by.testtask.taskmanager.dto.entityDto.UserDto;
import by.testtask.taskmanager.service.AccountService;
import by.testtask.taskmanager.service.UserService;
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
    public List<UserDto> getUsers(@RequestParam(required = false) LocalDate dateOfBirth, @RequestParam(required = false) String phone, @RequestParam(required = false) String name, @RequestParam(required = false) String email, @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return userService.searchUsers(dateOfBirth, phone, name, email, PageRequest.of(page,size));
    }

    @PostMapping("/phone")
    public UserDto addPhone(@RequestBody String phone,HttpServletRequest httpRequest) {
        return userService.addPhone(phone, jwtFilter.extractTokenFromCookie(httpRequest));
    }
    @PutMapping("/phone")
    public UserDto updatePhone(@Valid @RequestBody PhoneDataDto phoneDataDto, HttpServletRequest httpRequest) {
        return userService.updatePhone(phoneDataDto, jwtFilter.extractTokenFromCookie(httpRequest));
    }
    @DeleteMapping("/phone")
    public UserDto deletePhone(@RequestBody Long phoneId,HttpServletRequest httpRequest) {
        return userService.deletePhone(phoneId, jwtFilter.extractTokenFromCookie(httpRequest));
    }
    @PostMapping("/email")
    public UserDto addEmail(@RequestBody String email,HttpServletRequest httpRequest) {
        return userService.addEmail(email, jwtFilter.extractTokenFromCookie(httpRequest));
    }
    @PutMapping("/email")
    public UserDto updateEmail(@Valid @RequestBody EmailDataDto emailDataDto, HttpServletRequest httpRequest) {
        return userService.updateEmail(emailDataDto, jwtFilter.extractTokenFromCookie(httpRequest));
    }
    @DeleteMapping("/email")
    public UserDto deleteEmail(@RequestBody Long emailId,HttpServletRequest httpRequest) {
        return userService.deleteEmail(emailId,jwtFilter.extractTokenFromCookie(httpRequest));
    }

    @PostMapping("/transfer")
    public void transfer(@Valid @RequestBody TransferDto transferDto, HttpServletRequest httpRequest) throws AccountNotFoundException {
        accountService.transfer(transferDto, jwtFilter.extractTokenFromCookie(httpRequest));
    }
}
