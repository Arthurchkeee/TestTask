package by.bank.repository.specification;

import by.bank.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<User> buildUserSpecification(LocalDate dateOfBirth, String phone, String name, String email) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dateOfBirth != null) {
                predicates.add(cb.greaterThan(root.get("dateOfBirth"), dateOfBirth));
            }
            if (phone != null) {
                predicates.add(cb.equal(root.get("phone"), phone));
            }
            if (name != null) {
                predicates.add(cb.like(root.get("name"), name + "%"));
            }
            if (email != null) {
                predicates.add(cb.equal(root.get("email"), email));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

