package by.testtask.taskmanager.repo;

import by.testtask.taskmanager.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface PhoneDataRepo extends JpaRepository<PhoneData, Long> {
    Optional<PhoneData> findByPhone(String phone);
}
