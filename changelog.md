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
