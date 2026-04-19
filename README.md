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
