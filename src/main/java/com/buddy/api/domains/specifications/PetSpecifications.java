package com.buddy.api.domains.specifications;

import com.buddy.api.domains.enums.AgeRange;
import com.buddy.api.domains.enums.Gender;
import com.buddy.api.domains.enums.Species;
import com.buddy.api.domains.enums.WeightRange;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PetSpecifications {

    private PetSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<PetEntity> withParams(PetSearchCriteriaRequest params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addPredicateIfNotNull(predicates, params.id(),
                    value -> criteriaBuilder.equal(root.get("id"), value));

            addPredicateIfNotNull(predicates, params.shelterId(),
                    value -> criteriaBuilder.equal(root.get("shelter").get("id"), value));

            addPredicateIfNotNull(predicates, params.name(),
                    value -> criteriaBuilder.like(root.get("name"), "%" + value + "%"));

            addPredicateIfNotNull(predicates, params.species(),
                    value -> criteriaBuilder.equal(root.get("specie"),
                            Species.valueOfDescription(value).getDescription()));

            addPredicateIfNotNull(predicates, params.gender(),
                    value -> criteriaBuilder.equal(root.get("gender"),
                            Gender.valueOfDescription(value).getDescription()));

            addPredicateIfNotNull(predicates, params.location(),
                    value -> criteriaBuilder.like(root.get("location"), "%" + value + "%"));

            addPredicateIfNotNull(predicates, params.weightRange(), value -> {
                WeightRange range = WeightRange.fromDescription(value);
                return criteriaBuilder.between(root.get("weight"), range.getMin(), range.getMax());
            });

            addPredicateIfNotNull(predicates, params.description(),
                    value -> criteriaBuilder.like(root.get("description"), "%" + value + "%"));

            // Filtragem por faixa de idade
            if (params.ageRange() != null) {
                AgeRange ageRange = AgeRange.fromDescription(params.ageRange());

                // Calculate minimum and maximum birth date based on age range
                LocalDate today = LocalDate.now();
                LocalDate minBirthDate = today.minusYears(ageRange.getMax());
                LocalDate maxBirthDate = today.minusYears(ageRange.getMin() - 1); // Adjust for potential negative min

                // Filter by birthDate between min and max (inclusive)
                predicates.add(criteriaBuilder.between(root.get("birthDate"), minBirthDate, maxBirthDate));
            }

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
