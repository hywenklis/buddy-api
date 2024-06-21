package com.buddy.api.domains.specifications;

import com.buddy.api.domains.enums.AgeRange;
import com.buddy.api.domains.enums.Gender;
import com.buddy.api.domains.enums.Species;
import com.buddy.api.domains.enums.WeightRange;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

// TODO: Refatorar essa classe pois está muito grande e complexa
public final class PetSpecifications {

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

            if (params.ageRange() != null) {
                AgeRange ageRange = AgeRange.fromDescription(params.ageRange());

                LocalDate today = LocalDate.now();
                LocalDate minBirthDate;
                LocalDate maxBirthDate;

                switch (ageRange.getDescription()) {
                    case "10+ anos" -> {
                        minBirthDate = today.minusYears(ageRange.getMin());
                        predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), minBirthDate)
                        );
                    }
                    case "0-1 anos" -> {
                        minBirthDate = today.minusYears(ageRange.getMax() + 1).plusDays(1);
                        maxBirthDate = today.minusYears(ageRange.getMin()).plusDays(1).minusDays(1); // até o dia atual
                        predicates.add(
                                criteriaBuilder.between(root.get("birthDate"), minBirthDate, maxBirthDate)
                        );
                    }
                    case "1-2 anos" -> {
                        minBirthDate = today.minusYears(ageRange.getMax() + 1).plusDays(1);
                        maxBirthDate = today.minusYears(ageRange.getMin()).plusDays(1).minusDays(1);
                        predicates.add(
                                criteriaBuilder.between(root.get("birthDate"), minBirthDate, maxBirthDate)
                        );
                    }
                    default -> {
                        minBirthDate = today.minusYears(ageRange.getMax() + 1).plusDays(1);
                        maxBirthDate = today.minusYears(ageRange.getMin()).plusDays(1).minusDays(1);
                        predicates.add(
                                criteriaBuilder.between(root.get("birthDate"), minBirthDate, maxBirthDate)
                        );
                    }
                }
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
