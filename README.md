# OreoLib

[![JitPack](https://jitpack.io/v/el211/OreoLib.svg)](https://jitpack.io/#el211/OreoLib)

OreoLib is a modular Java 17 utility library designed to make code read closer to plain English.
As of 2.0.0 it is split into small modules so you depend only on what you need — the core stays
zero-dependency.

```java
import static com.oreo.lib.Oreo.*;
```

## Modules

| Module | Package | Purpose | Extra dependency |
| --- | --- | --- | --- |
| `oreolib-core` | `com.oreo.lib` | Fluent utilities: when/match, Flow, clamp, files, async, cache, cooldown | none (zero-dep) |
| `oreolib-db` | `com.oreo.lib.db` | Annotation-driven ORM over JDBC (`@Entity`/`@Query`/`CrudRepository`) | JDBC driver (SQLite optional, bundled) |
| `oreolib-gdx` | `com.oreo.lib.gdx` | libGDX helpers: Prefs, Save, Assets, Scene2D UI | libGDX |
| `oreolib-ecs` | `com.oreo.lib.ecs` | Zero-dependency entity-component-system | none (zero-dep) |

## Requirements

- Java 17 or newer
- Maven 3.8 or newer when building from source

## Installation

Add the JitPack repository, then pick the modules you need. On JitPack the group id is
`com.github.el211.OreoLib` and the artifact id is the module name.

### Maven with JitPack

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- Core utilities (zero-dependency) -->
    <dependency>
        <groupId>com.github.el211.OreoLib</groupId>
        <artifactId>oreolib-core</artifactId>
        <version>v2.0.0</version>
    </dependency>

    <!-- Optional: ORM. Add a JDBC driver yourself (e.g. sqlite-jdbc, postgresql, mysql-connector-j) -->
    <dependency>
        <groupId>com.github.el211.OreoLib</groupId>
        <artifactId>oreolib-db</artifactId>
        <version>v2.0.0</version>
    </dependency>
</dependencies>
```

### Gradle with JitPack

```groovy
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation "com.github.el211.OreoLib:oreolib-core:v2.0.0"
    implementation "com.github.el211.OreoLib:oreolib-db:v2.0.0"   // optional
    implementation "com.github.el211.OreoLib:oreolib-gdx:v2.0.0"  // optional
    implementation "com.github.el211.OreoLib:oreolib-ecs:v2.0.0"  // optional
}
```

### Build from source

```bash
git clone https://github.com/el211/OreoLib.git
cd OreoLib
mvn clean install
```

Locally installed coordinates use group id `com.oreo` and the module artifact ids
(`oreolib-core`, `oreolib-db`, `oreolib-gdx`, `oreolib-ecs`) at version `2.0.0`.

## Databases (oreolib-db)

The ORM is built on plain JDBC, so it works with any JDBC database — you supply the driver:

```java
Db sqlite   = Db.sqlite("game.db");                                   // bundled convenience
Db postgres = Db.connect("jdbc:postgresql://localhost/app", "u", "p");
Db mysql    = Db.connect("jdbc:mysql://localhost/app", "u", "p");
Db mariadb  = Db.connect("jdbc:mariadb://localhost/app", "u", "p");
```

`Db.sql(...)`, `Repository`, and the `@Query` interfaces run against all of them. Note:
`createTable()` and `@GeneratedValue` auto-increment currently emit SQLite-flavoured DDL; on other
engines create the schema yourself (or use application-assigned ids such as `UUID`). MongoDB is not
supported — it is not a JDBC/SQL database and would require a separate module.

## English-style API

### Decisions

```java
when(player.isOnline())
    .then(() -> sendMessage(player, "Welcome!"))
    .otherwise(() -> warn("Player is offline"));
```

Value-based conditions and switch-like matching are also available:

```java
when(score)
    .is(value -> value >= 100, () -> out("Legend"))
    .is(value -> value >= 50, () -> out("Pro"))
    .otherwise(() -> out("Beginner"));

match(status)
    .caseOf(ONLINE, () -> out("Online"))
    .caseOf(OFFLINE, () -> out("Offline"))
    .otherwise(() -> out("Unknown"));
```

### Repetition and waiting

```java
repeat(5).times(() -> out("Hello"));
repeat(5).times(index -> out("Iteration", index));

waitFor(500).milliseconds();
waitFor(2).seconds();
```

The original forms remain available:

```java
repeat(5, () -> out("Hello"));
times(5, index -> out(index));
sleep(500);
```

### Attempts and retries

`attempt(...)` is lazy: the operation starts when a terminal method such as `get`, `orElse`, `success`, or `failure` is called.

```java
String data = attempt(() -> downloadData())
    .upTo(3).times()
    .waiting(500).milliseconds()
    .orElse("Unavailable");
```

```java
attempt(() -> saveData())
    .success(ignored -> success("Saved"))
    .failure(error -> warn(error.getMessage()));
```

The simpler retry APIs are still supported:

```java
String data = retry(3, () -> downloadData());

retry(3)
    .delay(500)
    .run(() -> reconnect());
```

### Collection pipelines

```java
List<String> admins = from(players)
    .where(Player::isAdmin)
    .map(Player::getName)
    .distinct()
    .sorted()
    .toList();
```

Pipelines support `where`, `map`, `distinct`, `sorted`, `take`, `skip`, `each`, `firstOr`, `count`, `any`, `all`, `list`, and `toList`.

### Files

```java
OreoFile config = file("config.txt")
    .writeText("enabled=true\n")
    .appendText("port=25565\n");

String content = config.readText().orElse("enabled=false");

if (config.exists()) {
    out(content);
}
```

The shorter `read`, `write`, and `append` methods remain available for direct operations.

### Readable checks

```java
if (is(status).equalTo("ONLINE")) {
    out("Ready");
}

boolean allowed = is(role).oneOf("ADMIN", "MODERATOR");
boolean validLevel = number(level).isBetween(1, 100);
boolean hasCoins = number(coins).isPositive();
```

Use `require(...)` when an invalid value should throw immediately:

```java
require("username", username)
    .notNull()
    .notBlank()
    .notEmpty();

require("age", age)
    .min(18)
    .max(120);
```

### Async tasks

```java
runAsync(() -> saveData())
    .whenDone(() -> success("Saved"))
    .whenFailed(error -> warn(error.getMessage()));

AsyncTask<String> task = supplyAsync(() -> loadData());
String data = task.await();
```

Use `future()` when you need the underlying `CompletableFuture`.

### Recurring tasks

```java
ScheduledTask autosave = every(5).minutes().run(() -> saveData());

// Stop it when it is no longer needed.
autosave.close();
```

Recurring tasks use a daemon scheduler, and each call returns a cancellable `ScheduledTask`.

### Random choices

```java
String winner = chooseOneFrom(players);
```

An empty collection throws `IllegalArgumentException` instead of returning an unexpected `null`.

### Settings

Settings are resolved from a matching Java system property first, followed by an environment variable. Environment names are also checked in uppercase underscore form, so `server.port` can resolve `SERVER_PORT`.

```java
int port = setting("server.port")
    .asInteger()
    .orElse(25565);

boolean debug = setting("debug")
    .asBoolean()
    .orElse(false);
```

Supported conversions are `asText`, `asInteger`, `asLong`, `asDouble`, and `asBoolean`.

## Additional utilities

### Console output

```java
out("Player:", name, "Level:", level);
outf("Money: %.2f", money);
info("Started");
success("Saved");
warn("Low memory");
error("Failed");
```

### Null-safe chains

```java
String name = safe(player)
    .map(Player::getProfile)
    .map(Profile::getName)
    .orElse("Unknown");

String first = firstNonNull(databaseName, configName, "Steve");
```

### Lists and maps

```java
List<String> names = list("Alex", "Steve", "Elias");

Map<String, Integer> fruit = mapOf(
    "apple", 5,
    "banana", 10,
    "orange", 4
);
```

### Text

```java
String value = text("  HELLO OREO WORLD  ")
    .trim()
    .lower()
    .camelCase()
    .get();
```

### Cache and cooldowns

```java
OreoCache<UUID, User> users = Oreo.<UUID, User>cache()
    .expireAfter(Duration.ofMinutes(10));

User user = users.get(uuid, () -> database.load(uuid));
```

```java
Cooldown<UUID> teleport = Oreo.<UUID>cooldown(30, TimeUnit.SECONDS);

if (teleport.use(player.getUniqueId())) {
    teleport(player);
} else {
    out("Wait", teleport.remaining(player.getUniqueId()));
}
```

## API overview

| Type | Purpose |
| --- | --- |
| `Oreo` | Static entry point for the short API |
| `WhenCondition` / `WhenValue<T>` | Fluent true/false and value-based decisions |
| `Match<T>` | Switch-like matching |
| `Repetition` / `Delay` | Readable repetition and waiting |
| `Attempt<T>` / `Retry` | Exception handling, fallbacks, and retries |
| `Flow<T>` | Collection pipelines |
| `OreoFile` | Fluent UTF-8 file operations |
| `ValueCheck<T>` / `NumberCheck<T>` | Readable boolean checks |
| `Requirement<T>` / `Validation` | Fail-fast or collected validation |
| `AsyncTask<T>` | Async completion and failure callbacks |
| `ScheduledTask` | Cancellable recurring tasks |
| `Setting` | Typed system property and environment lookup |
| `Safe<T>` | Null-safe value chaining |
| `Text` | Fluent text transformations |
| `OreoCache<K,V>` | Optional-expiry cache |
| `Cooldown<K>` | Keyed cooldowns |

## Backward compatibility

OreoLib 2.0.0 keeps every helper from the 1.x line; the only breaking change is packaging — the
single `oreolib` artifact is now split into `oreolib-core`/`-db`/`-gdx`/`-ecs`. The `com.oreo.lib`
APIs (`out`, `text`, `list`, `range`, `chain`, `read`, `write`, `append`, `tryRun`, `tryGet`,
`retry`, `repeat`, `sleep`, `async`, `where`, `map`, `each`, `match`, `safe`, validation, cache,
cooldown, plus 1.3's `clamp`/`lerp`/`loop`/`sumBy` and the `Flow` aggregates) are unchanged and live
in `oreolib-core`.

## What's new in 1.3.0

Math and aggregate helpers for the cases plain Java makes verbose:

```java
health = clamp(health + regen, 0f, MAX_HEALTH);
float eased = lerp(start, end, t);
float barWidth = fraction(health, 0f, MAX_HEALTH); // 0..1

loop(36, i -> slots[i] = ItemStack.empty());        // index loop, no boxing

int total = sumBy(slots, ItemStack::count);
int online = countWhere(players, Player::isOnline);
```

`Flow` gained numeric and terminal operations so a pipeline stays one chain:

```java
int gold = from(slots).sumInt(ItemStack::count);
String names = from(players).map(Player::getName).join(", ");
Player top = from(players).maxBy(comparingInt(Player::getScore), null);
String last = from(history).reversed().firstOr("none");
```

New `Flow` methods: `sumInt`, `sumDouble`, `countWhere`, `join`, `maxBy`, `minBy`, `reversed`, `lastOr`, `toSet`.

## What's new in 2.0.0

- **Modular build** — four independent artifacts; depend only on what you need.
- **`oreolib-db`** — annotation-driven ORM: `@Entity`/`@Table`/`@Id`/`@Column`/`@GeneratedValue`/
  `@Enumerated`/`@Transient`, a `Repository<T>` with auto CRUD, `CrudRepository<T,ID>` you extend,
  and `@Query`/`@NativeQuery`/`@Modifying`/`@Param`/`@Procedure` repository interfaces. Works with
  any JDBC driver; UUID/enum/Instant mapping included.
- **`oreolib-gdx`** — `Prefs`, `Save`, `Assets`, and Scene2D `Ui` builders.
- **`oreolib-ecs`** — `Entity`, `Engine`, `EntitySystem`.

## Design goal

If an OreoLib helper is longer or less readable than normal Java, use normal Java. OreoLib removes boilerplate; it does not hide application logic.
