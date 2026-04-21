# PrismaticAPI

PrismaticAPI is a Bukkit/Paper text-formatting library focused on RGB colors, gradients, rainbow effects, MiniMessage compatibility, and legacy fallback.

It is designed for plugin code that needs one formatting pipeline but multiple output forms:

- Plain legacy strings for `Player#sendMessage(...)`
- Adventure components when MiniMessage is available
- Automatic downsampling for older client versions through VNC

## Highlights

- Multiple RGB syntaxes in one parser
- Gradient and rainbow tags
- Optional MiniMessage integration
- `RichText` results when you want both component and legacy output
- Version-aware fallback using VNC and ViaVersion-aware player checks

## Formatting pipeline

`PrismaticAPI` processes text in this order:

1. MiniMessage, when Adventure MiniMessage is present at runtime
2. Prismatic multi-color blocks such as gradients and rainbows
3. Single RGB color syntaxes
4. Legacy ampersand formatting such as `&a`, `&l`, and `&r`

This keeps MiniMessage and Prismatic tags from stepping on each other while still producing a final legacy string for Bukkit APIs.

## Supported formats

### Single RGB colors

- `{#ff8800}`
- `%#ff8800%`
- `[#ff8800]`
- `<#ff8800>`
- `&xff8800`
- `#ff8800`
- `&#ff8800`

### Gradients

- `<g:ff0000>Hello</g:0000ff>`
- `<gradient:ff0000>Hello</gradient:0000ff>`
- `<#ff0000>Hello</#0000ff>`
- `<#ff0000:#00ff00:#0000ff>Hello</gradient>`

### Rainbows

- `<rainbow:1>Hello</rainbow>`
- `<r:1>Hello</r>`

## Quick usage

### 1. Get a legacy string

```java
String raw = "<g:ff0000>Hello</g:0000ff> &lworld";
String formatted = PrismaticAPI.colorize(raw);
player.sendMessage(formatted);
```

### 2. Format for a specific player

```java
String raw = "<rainbow:1>Chromatic</rainbow>";
String formatted = PrismaticAPI.colorize(player, raw);
```

When ViaVersion is installed, PrismaticAPI uses the player's effective version to decide whether RGB can be preserved or should be downsampled.

### 3. Keep both component and legacy output

```java
RichText text = PrismaticAPI.colorizeText(player, "<#ff8800>PrismaticAPI");

player.sendMessage(text.asLegacy());
Component component = text.component();
```

### 4. Build colors programmatically

```java
String solid = PrismaticAPI.applyColor(new Color(255, 136, 0), "Hello", false);
String gradient = PrismaticAPI.applyGradient("Hello", Color.RED, Color.BLUE, false);
String rainbow = PrismaticAPI.applyRainbow("Hello", 1.0f, false);
```

## Useful API entry points

- `colorize(String)` returns a formatted legacy string
- `colorize(Player, String)` returns a formatted legacy string tailored to that player
- `colorizeText(...)` returns `RichText`
- `applyColorText(...)`, `applyGradientText(...)`, and `applyRainbowText(...)` return `RichText`
- `stripBukkit(...)`, `stripSpecial(...)`, `stripRGB(...)`, and `stripAll(...)` remove formatting in different levels
- `getStartColor(...)` and `getEndColor(...)` inspect the formatted output

## MiniMessage behavior

MiniMessage support is optional at runtime. If Adventure MiniMessage is available, PrismaticAPI will:

- deserialize standard MiniMessage tags
- preserve Prismatic gradient/rainbow sections by masking and restoring them safely
- downsample RGB output to named colors when a legacy target requires it

If MiniMessage is not present, the library still processes Prismatic formats and legacy codes normally.

## Requirements

- Java 8+
- Bukkit / Spigot / Paper API on the classpath
- Adventure MiniMessage is optional
- ViaVersion is optional

## Coordinates

If you publish the artifact to your own repository or consume it from a local build, the coordinates are:

```text
groupId:    me.croabeast
artifactId: PrismaticAPI
version:    1.3.0
```

## Local development

This project expects the `VNC` project to exist either:

- next to this repository as `../VNC`
- or inside this repository as `VNC`

The build automatically compiles the sibling `VNC` jar before compiling PrismaticAPI.

## Build

```bash
./gradlew jar
```
