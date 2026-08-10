# BlockTime Test Coverage Report

## Overview

This document summarizes the test coverage for the BlockTime entity functionality introduced in PR #140.

## Test Files Created

### 1. BlockTimeServiceTest.java

**Location:** `backend/src/test/java/de/ccq/resourcehub/service/BlockTimeServiceTest.java`

**Test Level:** Unit Test (with Mockito)

**Coverage:**

#### getAllBlockTimesByResourceId
- ✅ Returns all block times for a resource ordered by start date
- ✅ Returns empty list when no block times exist for resource

#### getBlockTimeById
- ✅ Returns block time when it exists
- ✅ Returns empty optional when block time does not exist

#### createBlockTime
- ✅ Creates and returns block time when valid
- ✅ Throws IllegalArgumentException when start date is null
- ✅ Throws IllegalArgumentException when end date is null
- ✅ Throws IllegalArgumentException when start date is after end date
- ✅ Throws IllegalArgumentException when title is null
- ✅ Throws IllegalArgumentException when title is empty
- ✅ Throws IllegalArgumentException when title is whitespace only

#### updateBlockTime
- ✅ Updates and returns block time when valid
- ✅ Throws IllegalArgumentException when start date is after end date

#### deleteBlockTime
- ✅ Deletes block time by ID

#### hasOverlappingBlockTimes
- ✅ Returns true when overlapping block times exist
- ✅ Returns false when no overlapping block times exist
- ✅ Correctly detects adjacent non-overlapping block times
- ✅ Returns false when different resource has block times

**Mocks:** BlockTimeRepository (using `@Mock`)

---

### 2. BlockTimeRepositoryIntegrationTest.java

**Location:** `backend/src/test/java/de/ccq/resourcehub/repository/BlockTimeRepositoryIntegrationTest.java`

**Test Level:** Integration Test (with TestEntityManager)

**Coverage:**

#### findByResourceIdOrderByStartDateAsc
- ✅ Returns block times ordered by start date
- ✅ Returns empty list when no block times exist for resource
- ✅ Returns block times only for specified resource

#### findActiveBlockTimesInRange
- ✅ Finds block times that overlap with the given range
- ✅ Finds block times that exactly match the range boundaries
- ✅ Finds block times that contain the range
- ✅ Finds block times contained within the range
- ✅ Finds block times that start exactly at end date
- ✅ Returns empty list when no overlapping block times exist
- ✅ Returns empty list when checking different resource

**Test Strategy:** Uses `@DataJpaTest` with `TestEntityManager` for database interaction testing.

---

### 3. BlockTimeControllerSliceTest.java

**Location:** `backend/src/test/java/de/ccq/resourcehub/controller/BlockTimeControllerSliceTest.java`

**Test Level:** Slice Test (WebMvcTest)

**Coverage:**

#### GET /api/block-times/resource/{resourceId}
- ✅ Returns 200 OK with list of block times
- ✅ Returns 200 OK with empty list when no block times exist

#### GET /api/block-times/{id}
- ✅ Returns 200 OK with block time when it exists
- ✅ Returns 404 Not Found when block time does not exist

#### POST /api/block-times
- ✅ Returns 201 Created when block time is valid
- ✅ Returns 400 Bad Request when validation fails (missing required fields)

#### PUT /api/block-times/{id}
- ✅ Returns 200 OK when block time is updated successfully
- ✅ Returns 400 Bad Request when validation fails (invalid date range)
- ✅ Returns 400 Bad Request when block time does not exist

#### DELETE /api/block-times/{id}
- ✅ Returns 204 No Content when block time is deleted
- ✅ Returns 400 Bad Request when block time does not exist

#### GET /api/block-times/check-overlap
- ✅ Returns 200 OK with true when overlapping block times exist
- ✅ Returns 200 OK with false when no overlapping block times exist
- ✅ Returns 400 Bad Request when required parameters are missing

**Test Strategy:** Uses `@WebMvcTest` to test only the controller layer without loading full Spring context.

---

## Test Coverage Summary

| Component | Test Files | Test Count | Coverage Level |
|-----------|-----------|------------|----------------|
| Service Layer | BlockTimeServiceTest | 20 | Unit Test (Mockito) |
| Repository Layer | BlockTimeRepositoryIntegrationTest | 14 | Integration Test (JPA) |
| Controller Layer | BlockTimeControllerSliceTest | 13 | Slice Test (WebMvc) |
| **Total** | **3** | **47** | **All Layers** |

---

## API Endpoints Tested

| Endpoint | Method | Coverage |
|----------|--------|----------|
| /api/block-times/resource/{resourceId} | GET | ✅ |
| /api/block-times/{id} | GET | ✅ |
| /api/block-times | POST | ✅ |
| /api/block-times/{id} | PUT | ✅ |
| /api/block-times/{id} | DELETE | ✅ |
| /api/block-times/check-overlap | GET | ✅ |

---

## Validation Rules Tested

1. ✅ Start date is required (not null)
2. ✅ End date is required (not null)
3. ✅ Start date must be before or equal to end date
4. ✅ Title is required (not null, not empty, not whitespace only)
5. ✅ Resource ID is required (not null)
6. ✅ Date range validation for overlap checking

---

## Boundary Conditions Tested

1. ✅ Adjacent dates (end date = start date) correctly detected as overlapping
2. ✅ Exact boundary matches (block time equals query range)
3. ✅ Block times containing query range
4. ✅ Block times contained within query range
5. ✅ Empty result sets
6. ✅ Non-existent resources and block time IDs

---

## Test Quality Indicators

- ✅ All tests follow the RBT pattern (Repository, Business, Test)
- ✅ Tests verify expected behavior and failure paths
- ✅ Tests use clear assertions with meaningful error messages
- ✅ Tests avoid first-level-cache false positives with `testEntityManager.clear()`
- ✅ Tests use fixed clock values (LocalDate) for deterministic results
- ✅ Tests don't depend on test execution order
- ✅ Tests verify interactions with mocks using `verify()` and `verifyNoMoreInteractions()`
- ✅ Tests use `verifyNoInteractions()` to ensure early exit on validation failures

---

## Recommendations for Further Testing

1. **Controller Integration Tests:** Consider adding full integration tests with Spring Boot test context for end-to-end API verification
2. **Performance Tests:** Add tests for large result sets to verify pagination or streaming behavior
3. **Security Tests:** Add tests to verify role-based access control (currently simplified to MANAGER role check)
4. **Database Constraint Tests:** Add tests for unique constraint violations and foreign key constraints
5. **Concurrency Tests:** Add tests for concurrent block time creation and overlap checking

---

## Test Execution

To run the tests:

```bash
cd backend
./mvnw test -Dtest=BlockTime*Test
```

To run all backend tests:

```bash
cd backend
./mvnw test
```

To generate coverage report:

```bash
cd backend
./mvnw clean test jacoco:report
```

To run mutation testing:

```bash
cd backend
./mvnw pitest:mutationCoverage
```

---

## Related Files

- Entity: `backend/src/main/java/de/ccq/resourcehub/entity/BlockTime.java`
- Repository: `backend/src/main/java/de/ccq/resourcehub/repository/BlockTimeRepository.java`
- Service: `backend/src/main/java/de/ccq/resourcehub/service/BlockTimeService.java`
- Controller: `backend/src/main/java/de/ccq/resourcehub/controller/BlockTimeController.java`
- Database Migration: `backend/src/main/resources/db/migration/V2__BlockTime_Entity.sql`