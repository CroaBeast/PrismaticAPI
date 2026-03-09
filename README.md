<p align="center">
  <a href="https://discord.com/invite/gzzhVqgy3b" alt="Support Server">
    <img alt="Discord" src="https://img.shields.io/discord/826555143398752286?style=for-the-badge&logo=discord&label=Support%20Server&color=635aea">
  </a>
  <img alt="Java 8+" src="https://img.shields.io/badge/Java-8%2B-orange?style=for-the-badge&logo=openjdk">
  <img alt="License GPLv3" src="https://img.shields.io/badge/License-GPLv3-blue?style=for-the-badge">
</p>

<h1 align="center">PrismaticAPI 🌈</h1>

<p align="center">
  Advanced color toolkit for <b>Bukkit / Spigot / Paper</b> plugins.<br>
  Gradients, rainbow effects, single RGB formats, MiniMessage-safe parsing, and legacy fallback in one place.
</p>

---

## ✨ Why PrismaticAPI?

PrismaticAPI started as a fork of [IridiumColorAPI](https://github.com/IridiumLLC/IridiumColorAPI), then evolved with a broader parser and a more complete formatting pipeline.

It helps you:

- Paint text with smooth gradients and rainbow effects.
- Parse multiple custom RGB syntaxes in a single pass.
- Keep compatibility with modern RGB and legacy 16-color clients.
- Convert output to plain strings or Adventure `TextComponent`.
- Strip any formatting quickly when you need clean text.

---

## 🧠 Processing Pipeline

`PrismaticAPI.colorize(...)` processes text in this exact order:

1. MiniMessage parsing (only if MiniMessage is available at runtime)
2. Multi-color formats (`gradient` and `rainbow`)
3. Single-color RGB formats
4. Legacy ampersand translation (`&a`, `&l`, etc.)

This order keeps complex tags stable and avoids conflicts between format types.

---

## 🎨 Supported Color Formats

All custom Prismatic formats are case-insensitive and use 6-digit hex (`RRGGBB`).

### Gradient 🟣🔵🟢

| Format | Description |
| --- | --- |
| `<g:RRGGBB>text</g:RRGGBB>` | Short gradient tag |
| `<gradient:RRGGBB>text</gradient:RRGGBB>` | Long gradient tag |
| `<#RRGGBB>text</#RRGGBB>` | Hash-style gradient tag |
| `<#RRGGBB:#RRGGBB[:#RRGGBB...]>text</g>` | Multi-stop gradient with short close tag |
| `<#RRGGBB:#RRGGBB[:#RRGGBB...]>text</gradient>` | Multi-stop gradient with long close tag |

Gradient tags can include internal color stops:

- `<g:ff0000>Hello <g:00ff00>World</g:0000ff>`
- `<gradient:ff0000>Hello <gradient:00ff00>World</gradient:0000ff>`
- `<#ff0000>Hello <#00ff00>World</#0000ff>`

### Rainbow 🌈

| Format | Description |
| --- | --- |
| `<rainbow:NUMBER>text</rainbow>` | Full rainbow tag |
| `<r:NUMBER>text</r>` | Short rainbow tag |

`NUMBER` accepts 1 to 3 digits and is parsed as the saturation value.

### Single Color 🎯

| Format | Example |
| --- | --- |
| `{#RRGGBB}` | `{#ff8800}Hello` |
| `%#RRGGBB%` | `%#ff8800%Hello` |
| `[#RRGGBB]` | `[#ff8800]Hello` |
| `<#RRGGBB>` | `<#ff8800>Hello` |
| `&xRRGGBB` | `&xff8800Hello` |
| `#RRGGBB` | `#ff8800Hello` |
| `&#RRGGBB` | `&#ff8800Hello` |

---

## 🚀 Quick Usage

### 1. Colorize a message for a player

```java
Player player = Bukkit.getPlayer("a player reference");
String raw = "<g:ff0000>Hello <gradient:00ff00>world</gradient:0000ff> &l!";
String colored = PrismaticAPI.colorize(player, raw);
player.sendMessage(colored);
```

### 2. Get an Adventure component

```java
TextComponent component = PrismaticAPI.colorizeAsComponent("<rainbow:1>PrismaticAPI</rainbow>");
```

### 3. Strip formatting

```java
String raw = "&#00ff99Clean &lthis <rainbow:1>text</rainbow>";
String plain = PrismaticAPI.stripAll(raw);
```

---

## 🧰 API Highlights

| Method | Purpose |
| --- | --- |
| `colorize(Player, String)` | Full pipeline with client legacy detection |
| `colorize(String)` | Full pipeline without player context |
| `colorizeAsComponent(...)` | Returns Adventure `TextComponent` |
| `applyGradient(...)` | Programmatic gradient application |
| `applyRainbow(...)` | Programmatic rainbow application |
| `stripBukkit(...)` | Removes `&`/`§` legacy color markers |
| `stripSpecial(...)` | Removes formatting markers (`k-o`, `r`, etc.) |
| `stripRGB(...)` | Removes custom Prismatic RGB formats |
| `stripAll(...)` | Removes all known formatting |

---

## 📦 Installation

Replace `${version}` with your target release.

### Maven

```xml
<repositories>
    <repository>
        <id>croabeast-repo</id>
        <url>https://croabeast.github.io/repo/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.croabeast</groupId>
        <artifactId>PrismaticAPI</artifactId>
        <version>${version}</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven {
        url "https://croabeast.github.io/repo/"
    }
}

dependencies {
    implementation "me.croabeast:PrismaticAPI:${version}"
}
```

---

## ✅ Compatibility Notes

- Java target: `1.8`
- API base: Spigot `1.16.5` (compile dependency)
- Legacy fallback: enabled for old clients (and ViaVersion-aware when available)
- MiniMessage integration: optional at runtime

---

## 🙏 Credits

- Original inspiration: [IridiumColorAPI](https://github.com/IridiumLLC/IridiumColorAPI)
- Maintained and expanded by **CroaBeast**

---

## 📄 License

This project is licensed under the **GNU GPL v3**.

