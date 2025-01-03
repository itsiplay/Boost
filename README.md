<img src="https://i.imgur.com/l4ugoDD.png" alt="Boost SMP Logo">

# Boost Plugin

Boost plugin is a Paper Minecraft plugin for version 1.20.4 that enhances player-versus-player (PvP) gameplay by introducing a unique killstreak system. Originally designed for the Boost SMP, this plugin tracks kills and deaths to grant players custom abilities, called "Boosts," that grow stronger as they increase their killstreak and decreases when they die. The plugin works independently but pairs well with the **BoostWeapon** plugin for additional custom weapons which I have also created.

---

## Features

- **Custom Killstreak Abilities**: Gain special effects and abilities (Boosts) as you increase your killstreak, up to a maximum of 5.
- **Dynamic Tab List**: The server's tab list updates in real-time, displaying players Boost levels with corresponding colors.
- **Persistent Boost Tracking**: Boost levels are stored in the server's killstreak file and update automatically upon kills, deaths, or item use.
- **Withdrawable Boosts**: Players can withdraw a Boost into an item, which can be traded, stored, used on another player, or used on themselves to instantly apply a Boost.
- **Craftable Reroll Book**: A special item that allows players to randomly add 1–3 Boosts to their current level, with restrictions for high-level Boosts (4-5).  
  [View Reroll Book crafting recipe on Imgur](https://imgur.com/a/fGUyEQp).

---

## Boost Effects

| Boost Level | TAB List Number Color       | Effects                                   |
|-------------|-------------|-------------------------------------------|
| 1 Boost     | Green       | Luck 1, Hero of the Village 1, Speed 1        |
| 2 Boosts    | Light Purple| Attack Speed is set to 4.5               |
| 3 Boosts    | Blue        | Knockback Resistance is set to 0.5    |
| 4 Boosts    | Gold        | Health Boost 1                            |
| 5 Boosts    | Dark Red    | Strength 1                |


**Effect Duration:** All effects last infinitely until a player dies or gains a kill on another player. These effects are only modified when the player experiences a change in their killstreak / boost (via death or killing another player).

---

## Commands

| Command           | Description                                                                                                             | Usage                            |
|-------------------|-------------------------------------------------------------------------------------------------------------------------|----------------------------------|
| `/withdrawBoost`  | Converts a player's current Boost into an item. (Does not require operator access)                                       | `/withdrawBoost`                |
| `/Boost`          | Displays the Boost level of a player. (Does not require operator access)                                                | `/Boost <player>`               |
| `/BoostChange`    | Changes a player's Boost level. (Operator access only)                                                                  | `/BoostChange <player> <level>` |

---

## How It Works

1. **Gaining Boosts**: Players gain 1 Boost per kill (up to 5). Each Boost level grants specific effects.
2. **Losing Boosts**: Deaths (via players, mobs, or the environment) reduce a player's Boost level by 1.
3. **Tab List Updates**: The tab list dynamically displays a players Boost levels with color-coded numbers.
4. **Boost Persistence**: Boost levels are stored in the server's killstreak file, ensuring they persist across sessions.
5. **Boost Items**:  Players can withdraw a Boost into an item, which can be traded, used to grant Boosts to others, or used on themselves to instantly apply a Boost.
6. **Reroll Mechanics**: The Reroll Book gives players a chance to add 1–3 Boosts but can only be used if the current Boost level is below 4.

---

## Installation

1. Download the `Boost.jar` file.
2. Place the file into the `plugins` folder of your Paper server.
3. Restart or reload your server.

---

## Compatibility

- **Minecraft Version**: Paper 1.20.4 - 1.21.3
- **Dependencies**: Compatible with the BoostWeapon plugin for additional functionality which adds custom weapons which I have also created.

---

## License and Support

- This plugin is provided as-is under the MIT License.
- **No support is provided for future versions of Paper servers**. Use at your own discretion.

Enjoy the Boost plugin and bring exciting new dynamics to your Minecraft server!
