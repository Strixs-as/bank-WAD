package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.Role;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.util.Optional;

@Repository
public class RoleRepository extends GenericRepository<Role, Long> {
    public RoleRepository() {
        super(Role.class);
    }
    @Override
    protected Long getEntityId(Role entity) {
        return entity.getId();
    }
    public Optional<Role> findByName(String name) {
        try {
            return Optional.of(entityManager.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                    .setParameter("name", name)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
