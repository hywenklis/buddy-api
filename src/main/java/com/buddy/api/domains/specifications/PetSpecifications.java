package com.buddy.api.domains.specifications;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public class PetSpecifications {

    private PetSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<PetEntity> withParams(PetSearchCriteriaRequest params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addPredicateIfNotNull(
                    predicates, params.id(),
                    value -> criteriaBuilder.equal(root.get("id"), value));
            addPredicateIfNotNull(
                    predicates, params.shelterId(),
                    value -> criteriaBuilder.equal(root.get("shelter").get("id"), value));
            addPredicateIfNotNull(
                    predicates, params.name(),
                    value -> criteriaBuilder.like(root.get("name"), "%" + value + "%"));
            addPredicateIfNotNull(
                    predicates, params.specie(),
                    value -> criteriaBuilder.like(root.get("specie"), "%" + value + "%"));
            addPredicateIfNotNull(
                    predicates, params.sex(),
                    value -> criteriaBuilder.equal(root.get("sex"), value));
            addPredicateIfNotNull(
                    predicates, params.age(),
                    value -> criteriaBuilder.equal(root.get("age"), value));
            addPredicateIfNotNull(
                    predicates, params.birthDate(),
                    value -> criteriaBuilder.equal(root.get("birthDate"), value));
            addPredicateIfNotNull(
                    predicates, params.location(),
                    value -> criteriaBuilder.like(root.get("location"), "%" + value + "%"));
            addPredicateIfNotNull(
                    predicates, params.weight(),
                    value -> criteriaBuilder.equal(root.get("weight"), value));
            addPredicateIfNotNull(
                    predicates, params.description(),
                    value -> criteriaBuilder.like(root.get("description"), "%" + value + "%"));

            if (predicates.isEmpty()) {
                return null;
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static <T> void addPredicateIfNotNull(
            List<Predicate> predicates,
            T value,
            Function<T, Predicate> predicateFunction) {
        if (value != null) {
            predicates.add(predicateFunction.apply(value));
        }
    }
}
