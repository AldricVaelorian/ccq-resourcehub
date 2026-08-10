# Backend Tests

This directory contains automated tests for the ResourceHub backend application.

## Test Structure

```
backend/src/test/java/de/ccq/resourcehub/
├── controller/       # Controller slice tests (WebMvcTest)
├── repository/       # Repository integration tests (DataJpaTest)
├── service/          # Service unit tests (Mockito)
└── BlockTimeTestCoverage.md  # Test coverage documentation
```

## Test Types

### 1. Service Unit Tests (`service/`)
- Test business logic and service orchestration
- Use Mockito for dependency mocking
- Focus on pure business rules without HTTP layer

### 2. Repository Integration Tests (`repository/`)
- Test database queries and JPA behavior
- Use TestEntityManager for database interaction
- Verify query correctness and database constraints

### 3. Controller Slice Tests (`controller/`)
- Test HTTP endpoints without full Spring context
- Verify request/response handling, validation, and status codes
- Use MockMvc for HTTP simulation

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=BlockTimeServiceTest

# Run tests with coverage
./mvnw test jacoco:report

# Run mutation testing
./mvnw pitest:mutationCoverage
```

## Test Conventions

1. Use `@ExtendWith(MockitoExtension.class)` for Mockito tests
2. Use `@MockitoBean` instead of `@MockBean` for Spring Boot 4.x
3. Construct the system under test (`sut`) explicitly in `@BeforeEach`
4. Use `testEntityManager.clear()` before repository queries to avoid first-level-cache issues
5. Use Testcontainers for database integration tests when needed
6. Use fixed `Clock` or LocalDate values for deterministic tests
7. Avoid depending on test execution order
8. Verify interactions with `verify()` and `verifyNoMoreInteractions()`
9. Use `verifyNoInteractions()` for early exit validation
10. Test happy paths, failure paths, and boundary conditions

## Coverage Goals

- **JaCoCo Line Coverage:** 80% minimum
- **PITest Mutation Coverage:** 70% minimum

These thresholds are configured in the build and checked during CI/CD.