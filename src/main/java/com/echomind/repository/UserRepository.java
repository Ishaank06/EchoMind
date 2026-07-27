package com.echomind.repository;

import com.echomind.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entities.
 *
 * Why an interface with no implementation?
 * Spring Data JPA generates the implementation at runtime using a JDK proxy.
 * When Spring sees an interface extending JpaRepository, it:
 * 1. Creates a proxy class implementing this interface
 * 2. Routes method calls to SimpleJpaRepository (the default impl)
 * 3. Registers it as a Spring bean — so you can @Autowired it anywhere
 *
 * JpaRepository<User, UUID> means:
 * - User = the entity type this repository manages
 * - UUID = the type of the entity's @Id field
 *
 * You get these methods for free (no code needed):
 * - save(User) / saveAll(List<User>)
 * - findById(UUID) → Optional<User>
 * - findAll() → List<User>
 * - deleteById(UUID)
 * - count()
 * - existsById(UUID)
 * ...and more.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Derived query method — Spring Data parses the method name and generates
     * the SQL automatically:
     *   findByEmail(String email)  →  SELECT * FROM users WHERE email = ?
     *
     * Returns Optional because the email might not exist.
     * This will be useful in the auth phase to look up users during login,
     * and right now helps us check for duplicate emails before insert.
     *
     * Naming convention matters:
     *   findBy + FieldName  →  WHERE clause
     *   findByNameAndEmail  →  WHERE name = ? AND email = ?
     *   findByEmailContaining → WHERE email LIKE '%?%'
     */
    Optional<User> findByEmail(String email);
}
