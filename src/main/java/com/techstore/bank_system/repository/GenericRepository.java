package com.techstore.bank_system.repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public abstract class GenericRepository<T, ID> {
    protected Class<T> entityClass;

    @PersistenceContext
    protected EntityManager entityManager;

    protected GenericRepository() {
    }
    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Transactional
    public T save(T entity) {
        if (getEntityId(entity) == null) {
            entityManager.persist(entity);
            entityManager.flush();
            return entity;
        } else {
            T merged = entityManager.merge(entity);
            entityManager.flush();
            return merged;
        }
    }

    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return entityManager.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
    }

    @Transactional
    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Transactional
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }

    @Transactional(readOnly = true)
    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }
    protected abstract ID getEntityId(T entity);
}
