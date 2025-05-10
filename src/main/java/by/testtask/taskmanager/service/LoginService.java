package by.testtask.taskmanager.service;

import by.testtask.taskmanager.dto.LoginDto;
import by.testtask.taskmanager.entity.User;
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
