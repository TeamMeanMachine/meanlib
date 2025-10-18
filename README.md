<img width="200px" align="right" src="https://team2471.org/wp-content/uploads/2017/08/tmm-logo_new-300x300.png">

# Meanlib

Meanlib is a Kotlin FRC Robot utility library made by Team 2471 Mean Machine.


## Installation

Inside your robot project folder, clone meanlib from git:
```shell
git clone https://github.com/TeamMeanMachine/meanlib/
```

Add meanlib as a submodule:
```shell
git submodule add https://github.com/TeamMeanMachine/meanlib/
```

Checkout the current year's branch:
```shell
git checkout frc[CURRENT_YEAR] # ex: git checkout frc2026
```

Add meanlib to your build.gradle dependencies:
```groovy
dependencies {
    implementation(project(":meanlib"))
    ..
}
```

Add meanlib to the jar task in your build.gradle:
```groovy
jar {
    dependsOn(':meanlib:jar') // <- add me

    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    from sourceSets.main.allSource
    manifest GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
```

