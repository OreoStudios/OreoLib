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
