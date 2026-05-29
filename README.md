<div align="center">

# 🌈 PrismaticAPI

**The ultimate text-formatting engine for Bukkit/Paper plugins.**

RGB colors · Gradients · Rainbows · MiniMessage · Interactive Chat Components

[![Version](https://img.shields.io/badge/version-1.5.0-blueviolet?style=flat-square)](https://github.com/CroaBeast/PrismaticAPI)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5+-green?style=flat-square)](https://spigotmc.org)
[![Java](https://img.shields.io/badge/Java-8+-orange?style=flat-square)](https://java.com)
[![Adventure](https://img.shields.io/badge/Adventure-optional-blue?style=flat-square)](https://docs.advntr.dev)

</div>

---

## ✨ What Is PrismaticAPI?

PrismaticAPI is a Bukkit/Paper text-formatting library that gives your plugin **beautiful, modern text** without the headache. One shared engine powers both legacy Bukkit strings and Adventure components — gradients, rainbows, per-player hex fallback, MiniMessage compatibility and interactive click/hover events all included.

---

## 🚀 Features

- 🎨 **Multiple RGB syntaxes** — `{#ff8800}`, `%#ff8800%`, `[#ff8800]`, `<#ff8800>`, `&#ff8800`, `#ff8800`, `&xff8800`
- 🌈 **Gradient & Rainbow tags** — color transitions per character, across as many color stops as you want
- 💬 **MiniMessage integration** — mix Prismatic tags and MiniMessage tags in the same string (optional at runtime)
- 🔮 **Adventure support** — produce `net.kyori.adventure.text.Component` output from the same pipeline, fully optional
- 🕹️ **Interactive components** — click events, hover text, hover items, URL auto-detection
- 🧠 **Player-aware formatting** — VNC + ViaVersion integration to serve hex or legacy output depending on the player's Minecraft version
- 🛡️ **Safe by design** — Adventure is never required; the library boots cleanly even when it's absent
- ⚡ **Backwards compatible** — the classic `PrismaticAPI.colorize(...)` methods still work unchanged

---

## 📦 How to Import

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://croabeast.github.io/repo/")
}

dependencies {
    implementation("me.croabeast:PrismaticAPI:1.5.0")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    maven { url 'https://croabeast.github.io/repo/' }
}

dependencies {
    implementation 'me.croabeast:PrismaticAPI:1.5.0'
}
```

### Maven

```xml
<repository>
    <id>croabeast-repo</id>
    <url>https://croabeast.github.io/repo/</url>
</repository>

<dependency>
    <groupId>me.croabeast</groupId>
    <artifactId>PrismaticAPI</artifactId>
    <version>1.5.0</version>
</dependency>
```

> **Tip:** If your plugin uses `PrismaticAPI.adventure()`, keep the Adventure API on your compile classpath and make sure the required Adventure runtime classes are present at startup.

---

## 🧩 Supported Syntax

### 🎨 Single RGB Colors

| Syntax | Example |
|--------|---------|
| Curly braces | `{#ff8800}` |
| Percent signs | `%#ff8800%` |
| Square brackets | `[#ff8800]` |
| Angle brackets | `<#ff8800>` |
| BungeeCord hex | `&xff8800` |
| Plain hex | `#ff8800` |
| Ampersand hex | `&#ff8800` |

### 🌈 Gradients

```
<g:ff0000>Hello world</g:0000ff>
<gradient:ff0000>Hello world</gradient:0000ff>
<#ff0000>Hello world</#0000ff>
<#ff0000:#00ff00:#0000ff>Hello world</gradient>   ← multi-stop!
```

### 🌀 Rainbows

```
<rainbow:1>Hello world</rainbow>
<r:1>Hello world</r>
```

### 📜 Legacy Formatting

```
&a  green        &l  bold
&c  red          &n  underline
&6  gold         &o  italic
&r  reset        &k  obfuscated
```

### 📝 MiniMessage (when Adventure is present)

Standard MiniMessage tags (`<bold>`, `<red>`, `<gradient:...>`, etc.) can be freely mixed with Prismatic tags in the same string.

---

## 📖 Quick Start

### 🟢 Legacy strings (always safe)

```java
// Basic colorize
String colored = PrismaticAPI.colorize(player, "<g:ff0000>Hello</g:0000ff> &lworld!");
player.sendMessage(colored);

// Using the facade directly
Formatter<String> legacy = PrismaticAPI.legacy();

String gradient = legacy.applyGradient("Sunset",  new Color(255, 100, 0), new Color(255, 0, 100), false);
String rainbow  = legacy.applyRainbow("Colorful!", 1.0f, false);
String solid    = legacy.applyColor(new Color(0, 200, 255), "Aqua text", false);
```

### 🔵 Adventure components (optional)

```java
if (PrismaticAPI.isAdventureAvailable()) {
    Formatter<Component> adv = PrismaticAPI.adventure();
    Component component = adv.colorize(player, "<rainbow:1>PrismaticAPI</rainbow>");
    player.sendMessage(component); // Paper native API
}
```

### 🖱️ Interactive chat components

**Single component** — one piece of text with click + hover:

```java
BaseComponent[] msg = PrismaticAPI
        .chatComponent("<#ff8800>Click me!")
        .setClick("run", "/help")
        .setHover("&eOpen help menu<n>&7Uses Prismatic colors")
        .compile(player);

player.spigot().sendMessage(msg);
```

**Multi-component** — parse several interactive segments from markup:

```java
// Markup format: <action:"argument">text</text>
BaseComponent[] msg = PrismaticAPI
        .multiComponent(
            "<run:\"/spawn\">&aGo to Spawn</text>" +
            " &7| " +
            "<suggest:\"/msg \">&bSend a Message</text>"
        )
        .compile(player);

player.spigot().sendMessage(msg);
```

**Supported click actions in markup:** `execute` / `click` / `run` / `suggest` / `url` / `file` / `page` / `copy`

---

## 🕹️ Chat Components In Depth

PrismaticAPI offers two types of interactive chat components, both compiled to `BaseComponent[]` for Spigot/Bungee's `player.spigot().sendMessage(...)`.

---

### 📌 `ChatComponent` — single interactive segment

A `ChatComponent` wraps one raw message and lets you attach a **click event**, a **text hover** or an **item hover** to it.

```java
ChatComponent<?> comp = PrismaticAPI.chatComponent("<#ff8800>Hello!");

// attach events
comp.setClick(ChatComponent.Click.EXECUTE, "/spawn");
comp.setHover("&eTeleport to spawn\n&7Click to confirm");

// compile and send
player.spigot().sendMessage(comp.compile(player));
```

#### 🖱️ Click events

The `Click` enum lists every supported action. Each constant also accepts short string aliases via `setClick(String, String)`:

| Constant | String aliases | What it does |
|----------|---------------|--------------|
| `EXECUTE` | `execute`, `click`, `run`, `run_command` | Runs a command as the player |
| `OPEN_URL` | `open_url`, `url` | Opens a URL in the browser |
| `OPEN_FILE` | `open_file`, `file` | Opens a file on the client machine |
| `SUGGEST` | `suggest`, `suggest_command` | Inserts text into chat without sending |
| `CHANGE_PAGE` | `change_page`, `page` | Flips a book page |
| `CLIPBOARD` | `clipboard`, `copy`, `copy_to_clipboard` | Copies text to clipboard |

```java
// using the enum constant
comp.setClick(ChatComponent.Click.OPEN_URL, "https://example.com");

// using a string alias
comp.setClick("url", "https://example.com");

// compact "action:payload" shorthand
comp.setClick("run:/spawn");
```

#### 💬 Text hover

Hover text can be supplied as a `List<String>`, a vararg array or a single string. Lines are separated with `<n>` inside a single string:

```java
// list of lines
comp.setHover(List.of("&eLine one", "&7Line two"));

// varargs
comp.setHover("&eLine one", "&7Line two");

// single string with <n> separator
comp.setHover("&eLine one<n>&7Line two");
```

Prismatic color codes are applied to hover text at compile time using the same player-aware pipeline.

#### 📦 Item hover

Pass a raw SNBT/NBT JSON string, or a Base64-encoded payload prefixed with `b64:` to avoid escaping issues:

```java
// raw JSON
comp.setHoverItem("{id:\"minecraft:diamond_sword\",Count:1b}");

// base64-encoded (recommended for complex NBT)
comp.setHoverItem("b64:" + Base64.getEncoder().encodeToString(nbtJson.getBytes()));
```

#### 🔗 URL auto-detection

If the raw message contains a URL (starting with `http://`, `https://` or `www.`), an `OPEN_URL` click event is **attached automatically** during `compile()` — no need to call `setClick` manually.

---

### 📋 `MultiComponent` — composite interactive message

A `MultiComponent` parses a raw string into multiple segments, each of which can carry independent events. Segments without markup are treated as plain text; URLs in plain segments get auto-linked.

```java
MultiComponent multi = PrismaticAPI.multiComponent(
    "<run:\"/spawn\">&aGo to Spawn</text>" +
    " &7| " +
    "<suggest:\"/msg \">&bMessage a player</text>"
);

player.spigot().sendMessage(multi.compile(player));
```

#### 🏷️ Default markup format

```
<action:"argument">visible text</text>
<action:"arg1"|action2:"arg2">visible text</text>
```

- The **opening tag** holds one or two `action:"argument"` pairs separated by `|`.
- The **closing tag** is always `</text>`.
- Supported actions inside the tag: all click aliases from the table above, plus `hover` and `hover_item`.

```
<!-- click only -->
<run:"/spawn">Go to Spawn</text>

<!-- hover only -->
<hover:"&eThis is hover text<n>&7Second line">Hover over me</text>

<!-- item hover only -->
<hover_item:"{id:\"minecraft:diamond\",Count:1b}">A diamond</text>

<!-- click + hover (pipe-separated) -->
<run:"/spawn"|hover:"&eTeleport home">Go Home</text>

<!-- suggest + hover_item -->
<suggest:"/give "|hover_item:"b64:eyJpZCI6Imdia...">Give item</text>
```

#### 🔀 Multi-stop actions and methods

Beyond markup, you can also apply events programmatically after construction:

```java
MultiComponent multi = PrismaticAPI.multiComponent("Hello </text>World</text>");

// affects only the LAST segment
multi.setClick("run", "/last");
multi.setHover("Hover on last");

// affects ALL segments at once
multi.setClickToAll("run", "/all");
multi.setHoverToAll("Same hover on every segment");
multi.setHoverItemToAll("{id:\"minecraft:apple\",Count:1b}");

// append more text or components
multi.append(" &7— extra text");
multi.append(PrismaticAPI.chatComponent("&cAnother segment").setClick("url", "https://example.com"));

// deep copy
MultiComponent copy = multi.copy();

// serialize back to markup string
String markup = multi.toFormattedString();
```

#### 🎨 Color continuity

When a segment does not begin with an explicit color code, the **last color of the previous segment** is prepended automatically. This prevents unexpected white resets between segments:

```
"&aGreen text</text> and more text</text>"
                          ↑ automatically gets &a prepended
```

---

## ⚙️ Formatting Pipeline

PrismaticAPI processes text in this exact order:

```
Input string
    │
    ├─ 1. MiniMessage  ──────────── (only when Adventure is present)
    ├─ 2. Prismatic multi-color ─── gradients & rainbows
    ├─ 3. Single RGB codes ───────── {#ff8800}, <#ff8800>, &xff8800 …
    └─ 4. Legacy Bukkit codes ────── &a, &l, &r …
          │
          ▼
    Formatted output
```

This order lets MiniMessage and Prismatic tags coexist without requiring Adventure on every runtime.

---

## 🔧 Utility Methods

Both `legacy()` and `adventure()` facades expose the same helpers:

| Method | Description |
|--------|-------------|
| `fromString(hex)` | Parse a color token or hex string into `ChatColor` |
| `stripBukkit(string)` | Remove `&a`, `§a`-style codes |
| `stripSpecial(string)` | Remove bold, italic, underline, etc. |
| `stripRGB(string)` | Remove gradient / rainbow / single-RGB syntax |
| `stripMiniMessage(string)` | Remove MiniMessage tags |
| `stripAll(string)` | Remove everything — returns plain text |
| `startsWithColor(string)` | `true` if the formatted string begins with a color code |
| `getStartColor(string)` | First color code in the formatted string |
| `getEndColor(string)` | Last color code in the formatted string |

---

## ⚠️ Important Behavior Notes

### `colorize(String)` is conservative

When called **without** a `Player`, PrismaticAPI cannot detect whether the receiver supports hex, so it **downgrades to the nearest legacy color**. For player-aware output:

```java
// ✅ player-aware: preserves RGB when the player's version supports it
String formatted = PrismaticAPI.legacy().colorize(player, raw);

// ⚠️ no player context: legacy fallback always
String formatted = PrismaticAPI.colorize(raw);
```

### Adventure is always optional

PrismaticAPI runs perfectly without Adventure. Only `PrismaticAPI.adventure()` requires it. Guard the call:

```java
if (PrismaticAPI.isAdventureAvailable()) {
    // safe to call PrismaticAPI.adventure()
}
```

Required Adventure classes at runtime:
- `net.kyori.adventure.text.Component`
- `net.kyori.adventure.text.minimessage.MiniMessage`
- `net.kyori.adventure.text.minimessage.tag.resolver.TagResolver`
- `net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer`

---

## 🔄 Migration from 1.3.x

| Old API | New API |
|---------|---------|
| `PrismaticAPI.colorize(player, raw)` | Unchanged ✅ |
| `PrismaticAPI.applyGradient(...)` | Unchanged ✅ |
| `RichText text = PrismaticAPI.colorizeText(player, raw)` | `PrismaticAPI.adventure().colorize(player, raw)` |
| `text.component()` | Result of `adventure().colorize(...)` is already a `Component` |
| `applyColorText(...)` | `PrismaticAPI.adventure().applyColor(...)` |
| `applyGradientText(...)` | `PrismaticAPI.adventure().applyGradient(...)` |
| `applyRainbowText(...)` | `PrismaticAPI.adventure().applyRainbow(...)` |

---

## 🛠️ Building

```bash
./gradlew jar
```

This project depends on **VNC** (`me.croabeast.vnc:VNC:1.2.1`) from the CroaBeast Maven repository, which is automatically resolved during the build.

---

## 📄 License

See [LICENSE](LICENSE) for details.

---

<div align="center">

Made with 💜 by **CroaBeast**

</div>
