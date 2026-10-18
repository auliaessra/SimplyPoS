package com.simplypos.modules.auth.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.simplypos.entities.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class UserSearchRepository {

    @PersistenceContext
    private EntityManager em;

    public Page<User> search(Long storeId, String q, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ============ Data query ============
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.where(buildPredicates(cb, root, storeId, q));
        cq.orderBy(cb.asc(root.get("username")));

        // ============ Count query ============
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<User> countRoot = countCq.from(User.class);
        countCq.select(cb.count(countRoot));
        countCq.where(buildPredicates(cb, countRoot, storeId, q));

        Long total = em.createQuery(countCq).getSingleResult();
        List<User> content = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<User> root,
            Long storeId, String q) {
        List<Predicate> predicates = new ArrayList<>();

        if (storeId != null) {
            predicates.add(cb.equal(root.get("store").get("id"), storeId));
        }

        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.toLowerCase() + "%";
            Predicate byUsername = cb.like(cb.lower(root.get("username")), pattern);
            Predicate byFullName = cb.like(cb.lower(root.get("fullName")), pattern);
            predicates.add(cb.or(byUsername, byFullName));
        }

        return predicates.toArray(new Predicate[0]);
    }
}