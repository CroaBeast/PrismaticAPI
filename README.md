# PrismaticAPI

PrismaticAPI is a Bukkit/Paper text-formatting library for RGB colors, gradients, rainbows, MiniMessage-aware parsing and legacy-safe fallback.

Version `1.4.0` reorganizes the public API around two facades backed by the same formatting engine:

- `PrismaticAPI.legacy()` returns `Formatter<String>`
- `PrismaticAPI.adventure()` returns `Formatter<Component>`

The old top-level methods such as `PrismaticAPI.colorize(...)` and `PrismaticAPI.applyGradient(...)` still exist and now delegate to `legacy()` for compatibility.

## What Changed In 1.4.0

- Added `PrismaticAPI.legacy()` as the always-safe string formatter facade.
- Added `PrismaticAPI.adventure()` as the optional Adventure formatter facade.
- Added `PrismaticAPI.isAdventureAvailable()` to guard Adventure-only code paths.
- Removed `RichText`.
- Removed `colorizeText(...)`, `applyColorText(...)`, `applyGradientText(...)` and `applyRainbowText(...)`.
- Kept the existing legacy top-level helpers as compatibility delegates to `legacy()`.

## Features

- One formatting engine for legacy strings and Adventure components.
- Multiple single-color RGB syntaxes.
- Gradient and rainbow tags.
- Optional MiniMessage support at runtime.
- Player-aware legacy fallback through VNC/ViaVersion support.
- Safe startup on runtimes where Adventure is not present.

## Coordinates

```text
groupId:    me.croabeast
artifactId: PrismaticAPI
version:    1.4.0
```

Add the repository that hosts your published artifact, then depend on `me.croabeast:PrismaticAPI:1.4.0`.

If your plugin calls `PrismaticAPI.adventure()`, keep the Adventure API on your compile classpath and ensure the required Adventure runtime classes are present when the plugin starts.

## Supported Syntax

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

### Legacy formatting

- `&a`
- `&l`
- `&n`
- `&r`

### MiniMessage

When Adventure MiniMessage is present at runtime, standard MiniMessage tags can be mixed with Prismatic syntax in the same string.

## Formatting Pipeline

PrismaticAPI processes text in this order:

1. MiniMessage, when the Adventure runtime is available.
2. Prismatic multi-color blocks such as gradients and rainbows.
3. Single RGB syntaxes.
4. Legacy Bukkit formatting such as `&a`, `&l` and `&r`.

This lets MiniMessage and Prismatic tags coexist without forcing Adventure to be present on every runtime.

## API Overview

### `PrismaticAPI.legacy()`

`legacy()` returns `Formatter<String>`, which is always safe to use. It emits Bukkit/Bungee-compatible color-code strings.

```java
String raw = "<g:ff0000>Hello</g:0000ff> &lworld";
String formatted = PrismaticAPI.legacy().colorize(player, raw);
player.sendMessage(formatted);
```

### `PrismaticAPI.adventure()`

`adventure()` returns `Formatter<Component>` and uses the same Prismatic parser to build Adventure components.

Always guard this call when Adventure is optional:

```java
if (PrismaticAPI.isAdventureAvailable()) {
    Component component = PrismaticAPI.adventure().colorize(player, "<#ff8800>PrismaticAPI");
}
```

If Adventure is not available and you call `PrismaticAPI.adventure()` anyway, the method throws `IllegalStateException` with a controlled error message instead of crashing with `NoClassDefFoundError`.

### Top-level compatibility methods

The classic entry points still work:

```java
String formatted = PrismaticAPI.colorize(player, "<rainbow:1>Chromatic</rainbow>");
String gradient = PrismaticAPI.applyGradient("Hello", Color.RED, Color.BLUE, false);
```

These methods delegate to the `legacy()` facade.

## Important Behavior Notes

### `colorize(String)` is conservative

`PrismaticAPI.colorize(String)` and `PrismaticAPI.legacy().colorize(String)` call the formatter without a `Player` context.

That means PrismaticAPI cannot know whether the receiver supports hex colors, so it falls back to legacy-safe output.

If you want player-aware RGB preservation, call:

```java
String formatted = PrismaticAPI.legacy().colorize(player, raw);
```

If you want exact RGB output without a player capability check, use the explicit color methods with `legacy = false`:

```java
String solid = PrismaticAPI.legacy().applyColor(new Color(255, 136, 0), "Hello", false);
String gradient = PrismaticAPI.legacy().applyGradient("Hello", Color.RED, Color.BLUE, false);
String rainbow = PrismaticAPI.legacy().applyRainbow("Hello", 1.0f, false);
```

### Adventure is optional

PrismaticAPI can run perfectly fine without Adventure on the classpath as long as you stay on `legacy()` or the compatibility methods.

`PrismaticAPI.adventure()` requires these classes at runtime:

- `net.kyori.adventure.text.Component`
- `net.kyori.adventure.text.minimessage.MiniMessage`
- `net.kyori.adventure.text.minimessage.tag.resolver.TagResolver`
- `net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer`

### MiniMessage support is shared

Both facades use the same parsing pipeline. If MiniMessage is available:

- standard MiniMessage tags are deserialized first
- Prismatic gradients and rainbows are preserved safely during MiniMessage parsing
- output is downsampled when the target must remain legacy-safe

If MiniMessage is not available, Prismatic-specific formatting and legacy color codes still work normally.

## Migration From 1.3.x

### Before

```java
String legacy = PrismaticAPI.colorize(player, raw);
RichText text = PrismaticAPI.colorizeText(player, raw);
Component component = text.component();
```

### After

```java
String legacy = PrismaticAPI.colorize(player, raw);

if (PrismaticAPI.isAdventureAvailable()) {
    Component component = PrismaticAPI.adventure().colorize(player, raw);
}
```

Migration summary:

- Replace `RichText` usage with `PrismaticAPI.adventure()`.
- Replace `colorizeText(...)` with `PrismaticAPI.adventure().colorize(...)`.
- Replace `applyColorText(...)`, `applyGradientText(...)` and `applyRainbowText(...)` with the corresponding `adventure()` methods.
- Keep existing legacy string code unchanged if you only need Bukkit-style strings.

## Useful Utility Methods

Both facades expose the same helper methods:

- `fromString(...)`
- `stripBukkit(...)`
- `stripSpecial(...)`
- `stripRGB(...)`
- `stripAll(...)`
- `startsWithColor(...)`
- `getStartColor(...)`
- `getEndColor(...)`

These methods are useful for inspecting or cleaning already-formatted strings without duplicating parsing logic in downstream plugins.

## Local Development

This project expects the `VNC` project to exist either:

- next to this repository as `../VNC`
- or inside this repository as `VNC`

The build compiles the sibling `VNC` jar before compiling PrismaticAPI.

## Build

```bash
./gradlew jar
```
