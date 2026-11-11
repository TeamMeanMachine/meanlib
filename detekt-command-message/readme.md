Custom compiler check plugin to detect if a command is being unused.

Example:
```kotlin
fun childCommand(): Command = command("Child") {
    println("child") // ok
}

fun parentCommand(): Command = command("Parent") {
    childCommand() // fail Detekt since `await` isn't used
    await(childCommand()) // ok
}

fun parentCommand(): Command = Command.noRequirements {
    childCommand() // fail since `await` isn't used
    it.await(childCommand()) // ok
}.named("parent")

```
Prints this message to the console and fail to complete
```
This command is created but will never run.

To run a sequential command, it must be put inside an await(...):

    await(yourCommand())
```


## Installation

1. Paste this whole folder `detekt-command-message/` to your project's root directory.
2. Include `detekt-command-message` inside your `settings.gradle`:
```groovy
include(":detekt-command-message")
```
3. Add the `Detekt` plugin to your `build.gradle`:
```groovy
plugins {
    id "dev.detekt" version "2.0.0-alpha.3"
}
```
4. Add `detekt-command-message` as a Detekt plugin in your `build.gradle` dependencies:
```groovy
dependencies {
    detektPlugins(project(":detekt-command-message"))
}
```
5. Paste the following into your build.gradle:
```groovy
detekt {
    buildUponDefaultConfig = false
    allRules = false
    config.setFrom(files("config/detekt.yml"))
    ignoreFailures = false
}

tasks.withType(JavaExec).configureEach {
    dependsOn(tasks.named("detektMain"))
}
```
6. Create a `config/detekt.yml` folder and file in the root of your project with the following content:
```yaml
console-reports:
  active: true
  exclude:
    - 'ProjectStatisticsReport'
    - 'ComplexityReport'
    - 'IssuesReport'
    - 'FileBasedIssuesReport'

potential-bugs:
  IgnoredReturnValue:
    active: true
    restrictToConfig: true
    returnValueTypes:
      - 'org.wpilib.command3.Command'
```
7. Run `./gradlew detektMain` and verify it is working by pasting this example code in your code. It should error on the `childCommand()`:
```kotlin
fun childCommand(): Command = Command.noRequirements {
    println("child")
}.named("child")

fun parentCommand(): Command = Command.noRequirements {
    childCommand() // fail Detekt since `await` isn't used
    it.await(childCommand())
}.named("parent")
```