package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.lang.reflect.Proxy;

/**
 * Creates a working implementation of a Spring-Data-style repository interface
 * whose methods are annotated with {@link Query}, {@link NativeQuery},
 * {@link Modifying}, {@link Param} or {@link Procedure}.
 *
 * <pre>{@code
 * interface PlayerDao {
 *     @Query("SELECT * FROM players WHERE score >= :min")
 *     List<PlayerRow> topScorers(@Param("min") int min);
 *
 *     @Modifying
 *     @Query("UPDATE players SET score = score + :bonus WHERE name = :name")
 *     int award(@Param("name") String name, @Param("bonus") int bonus);
 * }
 *
 * PlayerDao dao = Repositories.create(PlayerDao.class, db);
 * }</pre>
 */
public final class Repositories {
    private Repositories() {}

    @SuppressWarnings("unchecked")
    public static <R> R create(Class<R> repositoryInterface, Db db) {
        if (!repositoryInterface.isInterface()) {
            throw new OreoException("Repository type must be an interface: " + repositoryInterface.getName());
        }
        return (R) Proxy.newProxyInstance(
            repositoryInterface.getClassLoader(),
            new Class<?>[]{repositoryInterface},
            new RepositoryHandler(db, repositoryInterface));
    }
}
