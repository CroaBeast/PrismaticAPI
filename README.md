# PrismaticAPI

PrismaticAPI is a powerful, versatile utility designed to enhance text formatting and color manipulation in your Bukkit/Spigot/Paper plugins. It provides a robust API for converting hexadecimal color codes to Bukkit's ChatColor objects, creating dynamic gradients and rainbow effects, and processing strings for both legacy and modern color formats.

---

## Overview

The **PrismaticAPI** package offers a suite of methods to:

- **Apply Color Effects:**  
  Apply gradient and rainbow effects to strings. These methods generate arrays of `ChatColor` objects that are then applied character-by-character to create smooth color transitions.

- **Colorize Text:**  
  Process input strings by applying registered color patterns, translating alternate color codes (using `&`), and adjusting output based on the player's client version.

- **Strip Formatting:**  
  Remove Bukkit, RGB, and special formatting codes from strings to yield plain text.

All these functionalities are provided in a single, unified API that makes it simple to integrate advanced color effects into chat messages, GUIs, logs, and more.

---

## Key Features

- **Color Mapping:**  
  Uses an immutable map to associate standard AWT `Color` objects with their corresponding Bukkit `ChatColor` codes.

- **Gradient Effects:**  
  Create smooth color gradients between two colors by generating an array of intermediary colors that can be applied to text.

- **Rainbow Effects:**  
  Dynamically generate a rainbow effect across a string based on a defined number of steps and a saturation parameter.

- **Legacy and Modern Support:**  
  Automatically adapts to legacy (16-color mode) and modern RGB color support based on the server version and player client.

- **Text Processing:**  
  Provides methods to apply all registered color patterns to text, as well as strip any color or formatting codes, ensuring clean plain text output when needed.

---

## Usage Examples

### Example 1: Colorizing a Chat Message

```java
package com.example.myplugin;

import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    // Example player (could be obtained from an event)
    Player player = Bukkit.getPlayer("a player reference");

    // Colorize a message using PrismaticAPI
    String message = "&aHello, &bworld!";
    String coloredMessage = PrismaticAPI.colorize(player, message);

    // Send the colorized message
    player.sendMessage(coloredMessage);
  }
}
```

### Example 2: Applying a Gradient Effect

```java
package com.example.myplugin;

import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Define the start and end colors for the gradient
        Color startColor = new Color(255, 0, 0);   // Red
        Color endColor = new Color(0, 0, 255);   // Blue
        
        // Apply a gradient effect to the text "Gradient Text"
        String gradientText = PrismaticAPI.applyGradient("Gradient Text", startColor, endColor, false);
        
        // Log the gradient text (or send it to a player)
        getLogger().info(gradientText);
    }
}
```

### Example 3: Stripping All Formatting

```java
package com.example.myplugin;

import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        String formattedText = "&aThis &btext &chas &dcolored &ewith &6codes";
        
        // Remove all color and formatting codes
        String plainText = PrismaticAPI.stripAll(formattedText);
        
        getLogger().info("Plain text: " + plainText);
    }
}
```

---

## Maven / Gradle Installation

To include PrismaticAPI to the project, add the following repository and dependency to your build configuration. Replace `${version}` with the desired version tag.

### Maven

Add the repository and dependency to your `pom.xml`:

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

Add the repository and dependency to your `build.gradle`:

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

Replace `${version}` with the appropriate module version.

---

## Conclusion

**PrismaticAPI** consolidates advanced color manipulation and text formatting functions into a single, easy-to-use API. Whether you need to create eye-catching gradients, implement dynamic rainbow effects, or simply clean up formatted text, PrismaticAPI provides the tools to do so effectively. Its support for both legacy and modern RGB formats ensures broad compatibility across different server versions and player clients.

Enhance your plugin’s visual presentation and user experience with PrismaticAPI!

Happy coding!  
— *CroaBeast*
