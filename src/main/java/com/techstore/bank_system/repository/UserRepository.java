package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.User;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.util.Optional;

@Repository
public class UserRepository extends GenericRepository<User, Long> {
    public UserRepository() {
        super(User.class);
    }
    @Override
    protected Long getEntityId(User entity) {
        return entity.getId();
    }
    public Optional<User> findByEmail(String email) {
        try {
            return Optional.of(entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public Optional<User> findByPassportNumber(String passportNumber) {
        try {
            return Optional.of(entityManager.createQuery("SELECT u FROM User u WHERE u.passportNumber = :passportNumber", User.class)
                    .setParameter("passportNumber", passportNumber)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        try {
            return Optional.of(entityManager.createQuery("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber", User.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
