# Contributing

## Build Guide

### 1. Make sure to clone repo with tags

### 2. Install Rust toolchain

See https://rustup.rs/

### 3. Rust library

Install dependencies for linux

```shell
# This is essential for build `rodio` dependency.
sudo apt install libasound2-dev libdbus-1-dev 
```

Gradle builds the library with cargo and generates the Kotlin bindings automatically
when compiling the JVM target, so there is nothing else to run by hand.
It builds in release mode by default; pass `-P rust.debug=true` for a faster debug build.

If you have old `libhachimi.*` / `hachimi.dll` files in `composeApp/src/jvmMain/resources`
from the previous manual setup, delete them.

### 4. Add properties in `./local.properties`

```properties
app.release.apiBaseUrl=https://api.hachimi.world
app.release.assetsBaseUrl=https://api.hachimi.world
app.dev.apiBaseUrl=https://your-api-host
app.dev.assetsBaseUrl=https://your-assets-host
```

### 5. Build and run JVM target

JDK Version: 21

```shell
./gradlew run
```
## Release

1. Write the release notes in `changelogs/x.y.z.md` on `dev`, with `## Features 新功能`, `## Enhancements 改进` and `## Fixes 修复` sections.
2. Tag that commit `vx.y.z` and push the tag. This starts the release: the workflow builds the tagged commit and publishes `changelogs/x.y.z.md` as the in-app changelog. Once every build succeeds, it also creates a GitHub Release for the tag with the same notes and all packages.
3. Open a PR from `dev` to `main` and merge it, to keep `main` at the latest release.

The release fails before anything is built if the changelog file is missing or empty, or if the tag is not on `dev`. The version code is the first-parent commit count, which is larger on `dev` than on `main`, so a tag on `main` would publish a smaller version code than the last release.
