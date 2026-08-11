package de.ccq.resourcehub.repository;

import de.ccq.resourcehub.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing user entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     *
     * @param username the username
     * @return the user, if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email.
     *
     * @param email the email
     * @return the user, if found
     */
    Optional<User> findByEmail(String email);
}