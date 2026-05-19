## 📦 Version 1.9.1

### 📝 Improvements
- Crystals held in the player's offhand are now also auto-updated on dismount or interaction.
- Other crystals are no longer auto-updated when interacting with an animal using a crystal.
- Performance optimizations for crystal auto-updating.


## 📦 Version 1.9.0

### ✨ New
- Companion positions are now automatically updated on dismount or interaction.
  - All crystals in the player's inventory linked to the animal are updated automatically.
  - This behavior can be configured separately for dismount and interaction events.
  - A crystal is **not updated** if the animal moved less than the configured distance threshold (32 blocks by default).
  - The threshold is configurable.

### 📝 Improvements
- Improved consistency of some tooltip texts.


## 📦 Version 1.8.0

### ✨ New
- You can now view a companion’s last known position in the crystal tooltip by holding *Alt*
- This feature can be disabled in the config

### 📝 Improvements
- Clean crystals with no bound animals can now stack up to 64.


## 📦 Version 1.7.3

### 🤝 Compatibility
- Improved compatibility with other mods by removing the custom crafting menu override.
- The crafting preview tooltip no longer shows bound animals before the item is crafted.
- Crafted crystals still correctly preserve all bound animal data.


## 📦 Version 1.7.1

### 📝 Improvements
- Renamed the `disallow_untamed` config option to `allow_untamed` for improved consistency with other config options.
  - ⚠ If you previously changed this setting from its default value, you may need to update it again after upgrading.


## 📦 Version 1.7.0

### ✨ New
- **Deep Call Crystal** behavior updated:
  - can now teleport animals across dimensions.
  - now requires the player to have at least 4 XP levels to call animals, regardless of distance or dimension.
  - consumes 4 XP levels if at least one animal was teleported from another dimension, otherwise only 1 XP level.
  - no XP is consumed if no animals were teleported from unloaded chunks or other dimensions (existing behavior).
  - cross-dimensional calls now last 4× longer by default.
  - added new config options to customize this behavior.

### 📝 Improvements
- **Soul Sand** and **Dirt Path** are now valid blocks for crystal use and animal teleportation.
- Added an overlay message when the player attempts to bind an animal that cannot be bound.
- **Striders** now always teleport into *Lava* if possible.

### 🛠️ Changes
- Reworked config structure.
  - **The new config structure is incompatible** with the older versions.
  - ⚠ Existing config settings will be reset to default values after updating.
  - You may need to reconfigure the mod settings.


## 📦 Version 1.6.0

### 📝 Improvements
- Companion location timestamps in Crystal tooltips now support both real-time and in-game time modes.
- Added a config option to control how relative time and freshness indicators are calculated.
- In-game time is now used by default for coordinate freshness display.
- Older Crystal data created before this update may temporarily show only absolute timestamps until coordinates are refreshed.


## 📦 Version 1.5.0

### 📝 Improvements
- Crystals can now be used on **Ice**, **Glass**, and other transparent blocks.
- Crystals now store when animal coordinates were last updated. Hold *Shift* to view.
- Animals in the tooltip are now color-coded based on coordinate status:
  - 🟢 Green — updated within the last 2 minutes
  - ⚪ White — updated within the last 20 minutes
  - ⚙ Gray — coordinates were updated earlier
  - ⚫ Dark gray — animal not found at last known position
  - Green/white highlighting can be disabled in the config


## 📦 Version 1.4.0

### 📝 Improvements
- XP is now only consumed if at least one animal actually gets teleported from an unloaded chunk.


## 📦 Version 1.3.1

### 📝 Improvements
- **Axolotls** can now only teleport into water.


## 📦 Version 1.3.0

### ✨ New
- Added a crafting recipe: **Deep Call Crystal** can now be unbound using **Flint**, removing all bound animals.

### 📝 Improvements
- Added an overlay message when a crystal has no bound animals to summon.


## 📦 Version 1.2.3

### 🐞 Fixed
- Crystal glint is now removed when the player takes the item into their hand (prevents glint from staying if the item was quickly moved away during summoning).


## 📦 Version 1.2.2

### 🐞 Fixed
- Fixed an issue where using a crystal did not update the animal's dimension.
  This could cause animals to be teleported to an incorrect dimension based on the last stored value.


## 📦 Version 1.2.1

### 🐞 Fixed
- Fixed an issue where the crystal's glint would sometimes not be removed after multiple uses.


## 📦 Version 1.2.0

### 📝 Improvements
- **Crystals** can now be used on blocks without collision (plants, snow) if the block below is solid.
- Blocks without collision no longer prevent animals from teleporting.
- Teleport particles now appear around the teleported animal instead of at the crystal’s use location.
- **Crystals** can no longer bind animals while on cooldown.
- *Fire*, *Powder Snow*, *Magma Blocks*, and *Sweet Berry Bushes* now prevent teleportation (*Striders* can still appear on *Magma Blocks*).


## 📦 Version 1.1.0

### 📝 Improvements
- Animals now follow the player after teleporting.
- Added distinct teleport safety rules for ground animals and *Striders*.
- Teleportation now ensures animals arrive safely.
- If no safe position is found nearby, the animal remains in place and an overlay message is displayed.


## 📦 Version 1.0.0

- Initial publishing
