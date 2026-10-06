package com.oreo.lib.db;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DbTest {
    @Test
    void ormStoresQueriesAndMapsToRecords() {
        record Score(String name, int points) {}

        try (Db db = Db.sqliteMemory()) {
            db.run("CREATE TABLE score(name TEXT, points INT)");

            long affected = db.sql("INSERT INTO score(name, points) VALUES(?, ?)")
                .params("Alex", 100).run();
            db.sql("INSERT INTO score(name, points) VALUES(?, ?)").params("Steve", 250).run();
            assertEquals(1, affected);

            List<Row> rows = db.query("SELECT name, points FROM score WHERE points >= ?", 150);
            assertEquals(1, rows.size());
            assertEquals("Steve", rows.get(0).getString("name"));
            assertEquals(250, rows.get(0).getInt("points"));

            List<Score> top = db.sql("SELECT name, points FROM score ORDER BY points DESC")
                .mapTo(Score.class);
            assertEquals(2, top.size());
            assertEquals(new Score("Steve", 250), top.get(0));
            assertEquals(new Score("Alex", 100), top.get(1));

            int total = db.queryFirst("SELECT SUM(points) AS total FROM score")
                .map(row -> row.getInt("total")).orElse(0);
            assertEquals(350, total);
        }
    }

    @Table(name = "players")
    static class PlayerRow {
        @Id Long id;
        @Column(name = "name") String name;
        int score;
        PlayerRow() {}
        PlayerRow(String name, int score) { this.name = name; this.score = score; }
    }

    interface PlayerDao {
        @Query("SELECT * FROM players WHERE score >= :min ORDER BY score DESC")
        List<PlayerRow> topScorers(@Param("min") int min);

        @Query("SELECT COUNT(*) FROM players")
        long total();

        @Modifying
        @Query("UPDATE players SET score = score + :bonus WHERE name = :name")
        int award(@Param("name") String name, @Param("bonus") int bonus);
    }

    @Test
    void repositoryInterfaceRunsQueryAnnotations() {
        try (Db db = Db.sqliteMemory()) {
            Repository<PlayerRow> players = db.repository(PlayerRow.class).createTable();
            players.save(new PlayerRow("Alex", 100));
            players.save(new PlayerRow("Steve", 250));
            players.save(new PlayerRow("Max", 300));

            PlayerDao dao = Repositories.create(PlayerDao.class, db);

            List<PlayerRow> top = dao.topScorers(250);
            assertEquals(2, top.size());
            assertEquals("Max", top.get(0).name);        // ordered DESC
            assertEquals(3L, dao.total());

            assertEquals(1, dao.award("Alex", 50));       // @Modifying UPDATE
            PlayerRow alex = dao.topScorers(0).stream()
                .filter(p -> p.name.equals("Alex")).findFirst().orElseThrow();
            assertEquals(150, alex.score);
        }
    }

    @Test
    void repositoryDoesSpringStyleCrudFromAnnotations() {
        try (Db db = Db.sqliteMemory()) {
            Repository<PlayerRow> players = db.repository(PlayerRow.class).createTable();

            PlayerRow saved = players.save(new PlayerRow("Alex", 100));
            assertNotNull(saved.id);                 // generated key written back
            assertTrue(saved.id > 0);
            players.save(new PlayerRow("Steve", 250));
            assertEquals(2, players.count());

            PlayerRow found = players.findById(saved.id).orElseThrow();
            assertEquals("Alex", found.name);
            assertEquals(100, found.score);

            found.score = 150;
            players.save(found);                     // UPDATE (id is set)
            assertEquals(150, players.findById(saved.id).orElseThrow().score);
            assertEquals(2, players.count());        // still two rows, no duplicate

            players.deleteById(saved.id);
            assertEquals(1, players.count());
        }
    }

    enum Role { ADMIN, OFFICER, GUEST }

    @Table(name = "officers")
    static class Officer {
        @Id UUID id;
        @Column(name = "email", nullable = false, unique = true) String email;
        @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false) Role role;
        @Column(name = "created_at", nullable = false) Instant createdAt;
        Officer() {}
        Officer(UUID id, String email, Role role, Instant createdAt) {
            this.id = id; this.email = email; this.role = role; this.createdAt = createdAt;
        }
    }

    interface OfficerRepo extends CrudRepository<Officer, UUID> {
        @Query("SELECT * FROM officers WHERE role = :role")
        List<Officer> byRole(@Param("role") String role);
    }

    @Test
    void crudRepositoryInterfaceGivesCrudPlusCustomQueries() {
        try (Db db = Db.sqliteMemory()) {
            OfficerRepo officers = Repositories.create(OfficerRepo.class, db);
            officers.createTable();

            UUID id = UUID.randomUUID();
            officers.save(new Officer(id, "a@kaiju.io", Role.ADMIN, Instant.parse("2026-01-01T00:00:00Z")));
            officers.save(new Officer(UUID.randomUUID(), "b@kaiju.io", Role.GUEST, Instant.parse("2026-01-02T00:00:00Z")));

            assertEquals(2, officers.count());                 // inherited CRUD
            assertTrue(officers.existsById(id));
            assertEquals("a@kaiju.io", officers.findById(id).orElseThrow().email);
            assertEquals(2, officers.findAll().size());
            assertEquals(1, officers.byRole("ADMIN").size());  // custom @Query

            officers.deleteById(id);
            assertEquals(1, officers.count());
        }
    }

    @Test
    void repositoryHandlesUuidEnumAndInstant() {
        try (Db db = Db.sqliteMemory()) {
            Repository<Officer> officers = db.repository(Officer.class).createTable();

            UUID id = UUID.randomUUID();
            Instant now = Instant.parse("2026-10-06T10:15:30Z");
            officers.save(new Officer(id, "chief@kaiju.io", Role.ADMIN, now)); // app-assigned UUID -> INSERT
            assertEquals(1, officers.count());
            assertTrue(officers.existsById(id));

            Officer found = officers.findById(id).orElseThrow();
            assertEquals(id, found.id);                 // UUID round-trip
            assertEquals("chief@kaiju.io", found.email);
            assertEquals(Role.ADMIN, found.role);       // enum round-trip
            assertEquals(now, found.createdAt);         // Instant round-trip

            found.role = Role.GUEST;
            officers.save(found);                        // row exists -> UPDATE
            assertEquals(1, officers.count());           // no duplicate
            assertEquals(Role.GUEST, officers.findById(id).orElseThrow().role);
        }
    }
}
