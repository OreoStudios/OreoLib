package com.oreo.lib.db;

import java.util.List;
import java.util.Optional;

/**
 * Spring-Data-style base interface: extend it to get standard CRUD for free,
 * and add your own {@link Query}/{@link NativeQuery}/{@link Modifying} methods.
 *
 * <pre>{@code
 * interface OfficerRepo extends CrudRepository<Officer, UUID> {
 *     @Query("SELECT * FROM officers WHERE role = :role")
 *     List<Officer> byRole(@Param("role") String role);
 * }
 *
 * OfficerRepo officers = Repositories.create(OfficerRepo.class, db);
 * officers.createTable();
 * officers.save(new Officer(...));
 * Optional<Officer> one = officers.findById(id);
 * }</pre>
 *
 * Note: method-name-derived queries (Spring's {@code findByEmailAndRole}) are
 * NOT supported — use {@code @Query} for anything beyond the methods below.
 */
public interface CrudRepository<T, ID> {
    /** Creates the backing table if it does not exist. (Not in Spring; handy here.) */
    void createTable();

    T save(T entity);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    List<T> findAll();

    long count();

    void deleteById(ID id);

    void delete(T entity);
}
