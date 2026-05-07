# OreoLib

OreoLib is a lightweight, dependency-free utility library for Java 17+ focused on making common Java code shorter without making it unreadable.

```java
import static com.oreo.lib.Oreo.*;
```

## Requirements

- Java 17 or newer
- Maven 3.8+ when building from source

## Installation

Download `oreolib-1.1.0.jar` from the latest GitHub release, or install the project locally:

```bash
git clone https://github.com/el211/OreoLib.git
cd OreoLib
mvn clean install
```

Then use:

```xml
<dependency>
    <groupId>com.oreo</groupId>
    <artifactId>oreolib</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Output

```java
out("Hello");
out("Player:", name, "Level:", level);
print("Loading...");
outf("Money: %.2f", money);
info("Started");
success("Saved");
warn("Low memory");
error("Failed");
```

## Short if / else

```java
when(player.isAdmin(),
    () -> out("Admin"),
    () -> out("Player")
);

String rank = choose(player.isAdmin(), "ADMIN", "PLAYER");
```

For else-if style logic around a value:

```java
when(score)
    .is(v -> v >= 100, () -> out("Legend"))
    .is(v -> v >= 50, () -> out("Pro"))
    .otherwise(() -> out("Beginner"));
```

## Short switch / case

```java
match(status)
    .caseOf(ONLINE, () -> out("Online"))
    .caseOf(OFFLINE, () -> out("Offline"))
    .caseOf(BANNED, () -> out("Banned"))
    .otherwise(() -> out("Unknown"));
```

Predicate-based matching is also supported:

```java
match(score)
    .caseWhen(v -> v >= 100, () -> out("Legend"))
    .caseWhen(v -> v >= 50, () -> out("Pro"))
    .otherwise(() -> out("Beginner"));
```

## Null-safe chains

```java
String name = safe(player)
    .map(Player::getProfile)
    .map(Profile::getName)
    .orElse("Unknown");
```

```java
String name = or(configName, "Steve");
String first = firstNonNull(databaseName, configName, "Steve");
```

## Collection pipelines

```java
List<String> names = from(players)
    .where(Player::isOnline)
    .where(p -> p.getLevel() >= 10)
    .map(Player::getName)
    .sorted()
    .list();
```

Pipeline helpers include `where`, `map`, `distinct`, `sorted`, `take`, `skip`, `each`, `firstOr`, `count`, `any`, `all`, and `list`.

## Short maps, lists and loops

```java
var names = list("Alex", "Steve", "Elias");

Map<String, Integer> fruit = mapOf(
    "apple", 5,
    "banana", 10,
    "orange", 4
);

repeat(5, () -> out("Hello"));
times(10, i -> out(i));
```

## Validation

Fail immediately:

```java
require("username", username)
    .notNull()
    .notBlank()
    .notEmpty();

require("age", age)
    .min(18)
    .max(120);
```

Collect multiple validation errors:

```java
validate()
    .notBlank(username, "Username")
    .min(age, 18, "Age")
    .check(passwordsMatch, "Passwords must match")
    .throwIfInvalid();
```

## Try/catch without boilerplate

```java
int port = attempt(() -> Integer.parseInt(value))
    .orElse(25565);
```

```java
attempt(() -> save())
    .success(v -> success("Saved"))
    .failure(e -> error(e.getMessage()));
```

The older `tryGet`, `tryRun`, `onSuccess`, and `onError` APIs remain available.

## Retry

```java
String data = retry(3, () -> loadData());
```

```java
String data = retry(5)
    .delay(1000)
    .get(() -> loadData());
```

```java
retry(3)
    .delay(500)
    .run(() -> reconnect());
```

## Cache

```java
OreoCache<UUID, User> users = Oreo.<UUID, User>cache()
    .expireAfter(Duration.ofMinutes(10));

User user = users.get(uuid, () -> database.load(uuid));
```

## Cooldowns

```java
Cooldown<UUID> teleport = Oreo.<UUID>cooldown(30, TimeUnit.SECONDS);

if (teleport.use(player.getUniqueId())) {
    teleport(player);
} else {
    out("Wait", teleport.remaining(player.getUniqueId()));
}
```

## Timing

```java
timed("Loading world", () -> loadWorld());
World world = timed("Loading world", () -> loadWorld());
```

## Files

```java
String value = read("config.txt");
write("config.txt", "hello");
append("config.txt", "world");
```

## Async and sleep

```java
async(() -> expensiveTask());
CompletableFuture<String> result = async(() -> loadSomething());
sleep(1000);
```

## Text helper

```java
String value = text("  HELLO OREO WORLD  ")
    .trim()
    .lower()
    .camelCase()
    .get();
```

## Main classes

| Type | Purpose |
| --- | --- |
| `Oreo` | Static facade for commonly used helpers |
| `Match<T>` | Compact switch-like matching |
| `WhenValue<T>` | Fluent if / else-if chains |
| `Safe<T>` | Null-safe value chaining |
| `Flow<T>` | Collection pipelines |
| `Requirement<T>` / `Validation` | Validation helpers |
| `OreoCache<K,V>` | Optional-expiry cache |
| `Cooldown<K>` | Keyed cooldowns |
| `Retry` / `Attempts` | Retry helpers |
| `Result<T>` | Exception-aware operation results |
| `Console` | Short console input/output |
| `Text` | Fluent text operations |
| `Lists` | Collection shortcuts |
| `OreoFiles` | UTF-8 file helpers |
| `Tasks` | Async and sleep helpers |
| `Stopwatch` | Timing |
| `Chain<T>` | Generic value chaining |

## Backward compatibility

OreoLib 1.1.0 keeps the 1.0.x APIs, including `out`, `text`, `list`, `range`, `chain`, `read`, `write`, `append`, `tryRun`, `tryGet`, `retry`, `repeat`, `sleep`, `async`, `where`, `map`, and `each`.

## Build from source

```bash
mvn clean package
```

The build creates:

- `target/oreolib-1.1.0.jar`
- `target/oreolib-1.1.0-sources.jar`
- `target/OreoLib-1.1.0.zip`

## Design goal

If an OreoLib helper is longer or less readable than normal Java, use normal Java instead. OreoLib is meant to remove boilerplate, not hide application logic.
