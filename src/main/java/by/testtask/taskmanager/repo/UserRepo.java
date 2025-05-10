package by.testtask.taskmanager.repo;

import by.testtask.taskmanager.entity.Account;
import by.testtask.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findUserById(Long id);
}
