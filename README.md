# OreoLib

OreoLib is a lightweight, dependency-free Java 17 utility library designed to make code read closer to plain English.

```java
import static com.oreo.lib.Oreo.*;
```

## Requirements

- Java 17 or newer
- Maven 3.8 or newer when building from source

## Installation

Download `oreolib-1.2.0.jar` from the [latest GitHub release](https://github.com/el211/OreoLib/releases/latest), or clone and install the project locally:

```bash
git clone https://github.com/el211/OreoLib.git
cd OreoLib
mvn clean install
```

Then add it to your Maven project:

```xml
<dependency>
    <groupId>com.oreo</groupId>
    <artifactId>oreolib</artifactId>
    <version>1.2.0</version>
</dependency>
```

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
