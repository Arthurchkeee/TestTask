package by.testtask.taskmanager.repo;

import by.testtask.taskmanager.entity.EmailData;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface EmailDataRepo extends JpaRepository<EmailData, Long> {
    Optional<EmailData> findByEmail(String email);
}
