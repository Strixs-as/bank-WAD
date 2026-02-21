package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.GenericEntity;import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    public T save(T entity) {
        if (getEntityId(entity) == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }
    public List<T> findAll() {
        return entityManager.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
    }
    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }
    protected abstract ID getEntityId(T entity);
}
