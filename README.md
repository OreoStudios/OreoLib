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

