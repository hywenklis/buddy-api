package com.buddy.api.domains.specifications;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PetSpecifications {
    public static Specification<PetEntity> withParams(PetSearchCriteriaRequest params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (params.id() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), params.id()));
            }
            if (params.shelterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("shelter").get("id"), params.shelterId()));
            }
            if (params.name() != null) {
                // Supondo que name seja String
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + params.name() + "%"));
            }
            if (params.specie() != null) {
                predicates.add(criteriaBuilder.like(root.get("specie"), "%" + params.specie() + "%"));
            }
            if (params.sex() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sex"), params.sex()));
            }
            if (params.age() != null) {
                predicates.add(criteriaBuilder.equal(root.get("age"), params.age()));
            }
            if (params.birthDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("birthDate"), params.birthDate()));
            }
            if (params.location() != null) {
                predicates.add(criteriaBuilder.like(root.get("location"), "%" + params.location() + "%"));
            }
            if (params.weight() != null) {
                predicates.add(criteriaBuilder.equal(root.get("weight"), params.weight()));
            }
            if (params.description() != null) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + params.description() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


