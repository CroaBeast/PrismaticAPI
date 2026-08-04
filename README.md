<div align="center">

# 🌈 PrismaticAPI

**The ultimate text-formatting engine for Bukkit/Paper plugins.**

RGB colors · Gradients · Rainbows · MiniMessage · Interactive Messages

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
- 🕹️ **Interactive messages** — `Element`: parsed once, click events, hover text, hover items, placeholders, URL auto-detection
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

### 🖱️ Interactive messages

`Element` is the single message type. Parse once, render per player:

```java
// Markup format: <action:"argument">text</text>
Element menu = Element.parse(
        "<run:\"/spawn\">&aGo to Spawn</text>" +
        " &7| " +
        "<suggest:\"/msg \">&bSend a Message</text>"
);

player.spigot().sendMessage(menu.bungee(player));
player.sendMessage(menu.legacy(player));
```

Build one by hand:

```java
Element button = Element.text("Click me!")
        .toBuilder()
        .color("#ff8800")
        .click("run", "/help")
        .hover("&eOpen help menu", "&7Uses Prismatic colors")
        .build();

player.spigot().sendMessage(button.bungee(player));
```

**Supported click actions in markup:** `execute` / `click` / `run` / `suggest` / `url` / `file` / `page` / `copy`

---

## 🕹️ `Element` In Depth

`Element` is the single message type. It is **immutable and parsed once**: the interactive markup, the bare URLs and the placeholder tokens are located at parse time, and every later render reuses that work. Keep one instance per config entry and hand it to every player.

```java
Element line = Element.parse("<g:ff8800>Welcome</g:00ffaa> &7{player}");

player.sendMessage(line.legacy(player));                 // legacy String
player.spigot().sendMessage(line.bungee(player));        // BaseComponent[]
```

Adventure output lives in a separate class on purpose, so a server without the Adventure runtime never loads a Kyori class by touching an element:

```java
if (PrismaticAPI.isAdventureAvailable()) {
    Component c = AdventureElement.render(line, player);
}
```

---

### 🏗️ Building

Mutation goes through `Element.Builder`, so a shared instance can never change underneath another consumer. Style and event calls apply to the **last appended segment**.

```java
Element button = Element.builder()
        .append("Spawn")
        .color("#00ffaa")
        .bold()
        .click(Click.RUN_COMMAND, "/spawn")
        .hover("&7Go to spawn", "&8/spawn")
        .build();

// derive from an existing element without touching it
Element louder = button.toBuilder().append(" &c!").build();
```

| Builder method | What it does |
|----------------|--------------|
| `append(String)` | Adds literal text as a new segment |
| `appendMarkup(String)` | Parses interactive markup and adds the segments |
| `append(Element)` | Adds the segments of another element |
| `raw(String)` | Replaces the text of the last segment, keeping its events |
| `color` / `bold` / `italic` / `underlined` / `strikethrough` | Styles the last segment |
| `click` / `hover` / `hoverItem` | Attaches events to the last segment |
| `clickAll` / `hoverAll` | Applies an event to every segment |
| `clearClick` / `clearHover` | Removes an event from the last segment |
| `autoLinkUrl()` | Gives the last segment an `OPEN_URL` click from the first URL in its text |

---

### 🖱️ Click events

The `Click` enum carries no platform type: the mapping to Bungee or Adventure happens inside the matching renderer.

| Constant | String aliases | What it does |
|----------|---------------|--------------|
| `RUN_COMMAND` | `execute`, `click`, `run`, `run_command` | Runs a command as the player |
| `OPEN_URL` | `open_url`, `url` | Opens a URL in the browser |
| `OPEN_FILE` | `open_file`, `file` | Opens a file on the client machine |
| `SUGGEST_COMMAND` | `suggest`, `suggest_command` | Inserts text into chat without sending |
| `CHANGE_PAGE` | `change_page`, `page` | Flips a book page |
| `COPY_TO_CLIPBOARD` | `clipboard`, `copy`, `copy_to_clipboard` | Copies text to clipboard |

```java
builder.click(Click.OPEN_URL, "https://example.com");  // enum constant
builder.click("url", "https://example.com");           // string alias
builder.click("run:/spawn");                           // compact "action:payload"
```

An unknown or blank alias resolves to `SUGGEST_COMMAND`.

---

### 💬 Hover

Text hover accepts varargs, a `List<String>`, or a single string using the `<n>` separator:

```java
builder.hover("&eLine one", "&7Line two");
builder.hover(Arrays.asList("&eLine one", "&7Line two"));
builder.hover("&eLine one<n>&7Line two");
```

Item hover takes raw SNBT/NBT JSON, or a Base64 payload prefixed with `b64:` to avoid escaping issues:

```java
builder.hoverItem("{id:\"minecraft:diamond_sword\",Count:1b}");
builder.hoverItem("b64:" + Base64.getUrlEncoder().withoutPadding().encodeToString(nbt.getBytes(UTF_8)));
```

---

### 🏷️ Markup format

```
<action:"argument">visible text</text>
<action:"arg1"|action2:"arg2">visible text</text>
```

- The **opening tag** holds one or two `action:"argument"` pairs separated by `|`.
- The **closing tag** is always `</text>`.
- Supported actions: every click alias from the table above, plus `hover` and `hover_item`.

```
<!-- click only -->
<run:"/spawn">Go to Spawn</text>

<!-- hover only -->
<hover:"&eThis is hover text<n>&7Second line">Hover over me</text>

<!-- item hover only -->
<hover_item:"{id:\"minecraft:diamond\",Count:1b}">A diamond</text>

<!-- click + hover (pipe-separated) -->
<run:"/spawn"|hover:"&eTeleport home">Go Home</text>
```

Utilities around the markup:

```java
Element.isMarkup(raw);          // does the text contain interactive tags?
Element.stripMarkup(raw);       // drop the tags, keep the visible text
element.toMarkup();             // serialize back; re-parses to an equivalent element
```

#### Custom formats

`MarkupFormat` is the parsing strategy. Note that `accept` takes no player: parsing happens once and cannot depend on who receives the message.

```java
Element parsed = Element.parse(raw, myFormat);
```

---

### 🧩 Placeholders

Tokens shaped `{name}` and `%name%` survive parsing as data and are resolved at render time. This is what lets a gradient span the **resolved value** instead of the length of the token:

```java
Element line = Element.parse("<g:ff0000>Hello {player}</g:00ff00>");

RenderContext ctx = RenderContext.of(player, name ->
        "player".equals(name) ? player.getName() : null);

player.sendMessage(line.legacy(ctx));
```

A resolver returning `null` leaves the original token in place. Tokens starting with `#` are Prismatic color syntax, not placeholders, so `{#ff8800}` and `%#ff8800%` are never touched.

---

### ⚡ Render cache

There are only two color profiles, `Target.HEX` and `Target.LEGACY`, so each segment caches its colorized text in two slots. An element rendered to a hundred players pays the color pipeline twice.

The cache is skipped when a render actually resolves placeholders, or when a custom formatter is supplied via `RenderContext.withFormatter(...)`.

`toString()` returns the **raw** text and never renders — it is called by string concatenation, logging and debuggers, none of which should trigger a render or need a player.

---

### 🔗 URL auto-detection

`Element.parse` turns any bare URL (`http://`, `https://` or `www.`) in a non-markup span into its own segment with an `OPEN_URL` click. On a builder, call `autoLinkUrl()` explicitly. An explicit click always wins.

---

### 🎨 Color continuity

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

## 🔄 Migration from 1.5.x

The `me.croabeast.prismatic.chat` package is gone. `Element` replaces all of it.

| Old API | New API |
|---------|---------|
| `PrismaticAPI.chatComponent(raw)` | `Element.text(raw).toBuilder()` |
| `PrismaticAPI.multiComponent(raw)` | `Element.parse(raw)` |
| `ChatComponent` / `MultiComponent` | `Element` (one type; a single segment is not a special case) |
| `component.compile(player)` | `element.bungee(player)` |
| `component.setMessage(raw)` | `builder.raw(raw)` |
| `component.setClick(Click.EXECUTE, v)` | `builder.click(Click.RUN_COMMAND, v)` |
| `component.setHover(...)` | `builder.hover(...)` |
| `component.setHoverItem(json)` | `builder.hoverItem(json)` |
| `multi.setClickToAll(...)` | `builder.clickAll(...)` |
| `multi.setHoverToAll(...)` | `builder.hoverAll(...)` |
| `multi.append(raw)` | `builder.appendMarkup(raw)` |
| `multi.copy()` | Not needed; `Element` is immutable |
| `multi.toFormattedString()` | `element.toMarkup()` |
| `ChatFormat` | `MarkupFormat` (no `Player` parameter) |
| `DEFAULT_FORMAT.removeFormat(raw)` | `Element.stripMarkup(raw)` |
| `DEFAULT_FORMAT.isFormatted(raw)` | `Element.isMarkup(raw)` |
| `ChatComponent.URL_PATTERN` | `Element.URL_PATTERN` |
| `ChatProcessor.colorize` | `RenderContext.withFormatter(...)` |
| `ChatProcessor.prepare` | `MarkupFormat.prepare(...)` |
| `ChatComponent.Click.EXECUTE` | `Click.RUN_COMMAND` (string aliases unchanged) |
| `ChatComponent.Click.SUGGEST` | `Click.SUGGEST_COMMAND` |
| `ChatComponent.Click.CLIPBOARD` | `Click.COPY_TO_CLIPBOARD` |

Two behaviour changes worth knowing:

- **Mutation.** The old components mutated in place; `Element` is immutable. `element.toBuilder()...build()` returns a new instance, and the original is untouched.
- **Empty messages.** `compile()` documented a non-empty array and padded an empty message with a blank component. `Element.text("").bungee()` returns an empty array.

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
