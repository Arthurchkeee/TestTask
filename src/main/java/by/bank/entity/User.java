package by.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 500)
    private String name;
    private LocalDate dateOfBirth;
    @Column(length = 500)
    private String password;
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    private Account account;
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<EmailData> emails=new ArrayList<>();
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<PhoneData> phones=new ArrayList<>();


}
