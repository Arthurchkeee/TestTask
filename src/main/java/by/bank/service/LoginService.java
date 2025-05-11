package by.bank.service;

import by.bank.dto.LoginDto;
import by.bank.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {
    public boolean isValidCredentials(LoginDto request,Optional<User> user) {
        return user.map(u -> Objects.equals(request.getPassword(), u.getPassword())).orElse(false);
    }

}
