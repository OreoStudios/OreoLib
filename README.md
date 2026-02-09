# OreoLib

OreoLib is a lightweight, dependency-free utility library for Java 17 and newer. It provides concise helpers for console output, collections, text transformations, file operations, retries, asynchronous tasks, validation, and fluent pipelines.

The public API lives in `com.oreo.lib`. For the shortest syntax, statically import the `Oreo` facade:

```java
import static com.oreo.lib.Oreo.*;
```

## Requirements

- Java 17 or newer
- Maven 3.8 or newer (when building from source)

## Installation

### Download a release

Download `oreolib-1.0.1.jar` from the [latest GitHub release](https://github.com/el211/OreoLib/releases/latest), then add it to your project's classpath.

### Install with Maven

Clone the repository and install OreoLib in your local Maven repository:

```bash
git clone https://github.com/el211/OreoLib.git
cd OreoLib
mvn clean install
```

You can then add it to another Maven project:

```xml
<dependency>
    <groupId>com.oreo</groupId>
    <artifactId>oreolib</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Quick start

```java
import java.time.Duration;
import java.util.List;

import com.oreo.lib.Lists;

import static com.oreo.lib.Oreo.*;

public class App {
    public static void main(String[] args) {
        List<String> names = list("Elias", "Alex", "Emma", "Elias");
        List<String> shortNames = where(
                Lists.distinct(names),
                name -> name.length() <= 5
        );

        each(shortNames, name -> out("Hello", name));

        String value = retry(
                3,
                Duration.ofMillis(250),
                () -> loadValue()
        );

        success(text(value).trim().camelCase().get());
    }
}
```

## Features

### Console helpers

```java
out("Player:", name, "| Level:", level);
outf("Player: %s | Level: %d", name, level);
info("Server started");
success("Saved successfully");
warn("Low memory");
error("Database connection failed");
```

### Collections

```java
List<Integer> numbers = range(1, 6);
List<Integer> even = where(numbers, number -> number % 2 == 0);
List<String> labels = map(even, number -> "item-" + number);
```

### Text transformations

```java
String value = text("  Hello Oreo World  ")
        .trim()
        .camelCase()
        .get();
// helloOreoWorld
```

### Safe exception handling

```java
int port = tryGet(() -> Integer.parseInt(input))
        .orElse(25565);

tryRun(() -> saveSomething())
        .onSuccess(ignored -> success("Saved"))
        .onError(error -> error(error.getMessage()));
```

### File operations

```java
write("data/config.txt", "enabled=true\n");
append("data/config.txt", "port=25565\n");

String config = tryGet(() -> read("data/config.txt"))
        .orElse("enabled=false");
```

### Async tasks and timing

```java
async(() -> expensiveTask());
CompletableFuture<String> result = async(() -> loadSomething());

long elapsedMillis = Stopwatch.measure(() -> expensiveTask());
out("Completed in", elapsedMillis, "ms");
```

### Fluent pipelines

```java
String result = chain("  HELLO WORLD  ")
        .map(String::trim)
        .map(String::toLowerCase)
        .tap(Oreo::out)
        .get();
```

## API overview

| Type | Purpose |
| --- | --- |
| `Oreo` | Static facade for commonly used helpers |
| `Console` | Console input and formatted output |
| `Text` | Immutable fluent string transformations |
| `Lists` | Collection creation, filtering, mapping, ranges, and distinct values |
| `OreoFiles` | UTF-8 file reading, writing, and appending |
| `Result<T>` | Exception-aware operation results and fallbacks |
| `Attempts` | Retry operations with optional delays |
| `Tasks` | Sleep and `CompletableFuture` helpers |
| `Checks` | Argument and state validation |
| `Stopwatch` | Elapsed-time measurement |
| `Chain<T>` | Generic fluent value pipelines |

## Build from source

```bash
mvn clean package
```

The build creates:

- `target/oreolib-1.0.1.jar` — compiled library
- `target/oreolib-1.0.1-sources.jar` — source archive
- `target/OreoLib-1.0.1.zip` — release bundle containing the README and both JARs

## Design goal

OreoLib focuses on reducing repetitive utility code while keeping application, game, and plugin logic readable. It complements the Java standard library rather than replacing it.
