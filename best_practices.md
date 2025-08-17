
<b>Pattern 1: Normalize and defensively parse enum/string inputs by uppercasing with a fixed locale and falling back to a safe default to avoid case-sensitivity bugs and future enum growth issues.</b>

Example code before:
```
enum ClientType { WEB, MOBILE, TOOLS, UNKNOWN }
ClientType fromString(String s) {
  return Arrays.stream(ClientType.values())
    .filter(t -> t.name().equals(s))
    .findFirst().orElse(UNKNOWN);
}
```

Example code after:
```
enum ClientType { WEB, MOBILE, TOOLS, UNKNOWN }
ClientType fromString(String s) {
  if (s == null) return UNKNOWN;
  final var key = s.toUpperCase(Locale.ROOT);
  return Arrays.stream(ClientType.values())
    .filter(t -> t.name().equals(key))
    .findFirst().orElse(UNKNOWN);
}
```

<details><summary>Examples for relevant past discussions:</summary>

- https://github.com/hywenklis/buddy-api/pull/151#discussion_r1988137538
</details>


___

<b>Pattern 2: Enforce request validation rules precisely at the boundary (controllers/requests), including domain-specific constraints like past-or-present dates and trimmed/unique name checks, rather than broad generic validations.</b>

Example code before:
```
public record PetRequest(
  String name,
  String specie,
  String gender,
  LocalDate birthDate
) {}
```

Example code after:
```
public record PetRequest(
  @NotBlank String name,
  @NotBlank String specie,
  @NotBlank String gender,
  @PastOrPresent(message = "Birth date must be in the past or present")
  LocalDate birthDate
) {}
```

<details><summary>Examples for relevant past discussions:</summary>

- https://github.com/hywenklis/buddy-api/pull/101#discussion_r1765634680
- https://github.com/hywenklis/buddy-api/pull/101#discussion_r1765657430
- https://github.com/hywenklis/buddy-api/pull/135#discussion_r1866757604
</details>


___

<b>Pattern 3: Keep mapping logic out of DTOs by using dedicated mappers; reserve DTOs for data transport and move entity conversion to a mapper layer to prevent mixing responsibilities.</b>

Example code before:
```
public record AccountDto(String email, String phone, String password, Boolean consent) {
  public AccountEntity toEntity() {
    return new AccountEntity(null, email, phone, password, consent);
  }
}
```

Example code after:
```
public record AccountDto(String email, String phone, String password, Boolean consent) {}
@Mapper
interface AccountMapper {
  AccountEntity toEntity(AccountDto dto);
}
```

<details><summary>Examples for relevant past discussions:</summary>

- https://github.com/hywenklis/buddy-api/pull/123#discussion_r1848310011
</details>


___

<b>Pattern 4: Scope PRs to a single cohesive concern and avoid mixing unrelated changes to ensure easier review and safer future reversions.</b>

Example code before:
```
// PR adds DB model changes and also refactors unrelated adoption entity package names
```

Example code after:
```
// PR A: DB model changes
// PR B: Adoption entity refactor
```

<details><summary>Examples for relevant past discussions:</summary>

- https://github.com/hywenklis/buddy-api/pull/109#discussion_r1800247688
</details>


___
