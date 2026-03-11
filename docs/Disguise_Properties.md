# Disguise Properties

## Summary

Disguise Properties (Or properties) are a bunch of things that allows players to customize their disguise.

Players can specify properties via the `/morph <id> [properties]` and the `/modify-morph <properties>` command.

Input format for commands metioned above is `[key1=value1, key2=value2, key3=value3...]`. The `value` part accepts either double `"` and single `'` quotes.

## Built-in properties

Currently, FeatherMorph comes with support for these built-in properties:

### Entity Common

| Name | Description | Type of the value | Input Example |
|---|---|---|---|
| `entity/static_yaw` | The static yaw of the disguise | Float value from `-360` to `360` | `1`, `1.3`, `225.14` |
| `entity/static_pitch` | The static pitch of the disguise | Float value from `-90` to `90` | `17`, `-12.5` |
| `entity/static_pose` (!!!Experimental!!!) | The static Pose of the disguise | [Pose](https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/Pose.html) | `standing`, `sneaking`, `fall_flying` |

As the time writing this article (2025/3/9), the client mod hasn't add support for `entity/static_pose` yet.

### Living Entity

Based on: [`Entity Common`](#entity-common)

| Name | Description | Type of the value | Input Example |
|---|---|---|---|
| `entity/custom_name_visible` | Whether the Custom Name of the disguise is always visible. | Boolean | `true`, `false` |
| `entity/stucked_arrows` | Amount of arrows stick on the disguise, not all entities supports rendering this. | Integer from `0` to `100` | `1`, `10`, `100` |
| `entity/custom_name` | Custom Name of the disguise | Text Component | Plain text<br> A text with minimessage like `<rainbow>Name painted like a rainbow!`<br> A JSON-formatted text component like `{"text":"Hello"}` |
| `entity/equipment` | Equipment data of the disguise | A JSON record of `MorphEquipmentStruct` | `{"dataVersion": 4671, "equipmentData": {"mainhand":"{\"id\": \"minecraft:air\"}"}}` <br> *This is used for communication between the server and the client mod, I doubt if anyone could really read and write this thing.* |
| `entity/display_disguise_equipment` | Whether to display disguise equipment rather than player's own equipment | Boolean | `true`, `false` |
| `living_entity/static_health` | Static health of the disguise, set to `0` to make the disguise appears dead. | Float | `0`, `1.3` |

### Player

Based on: [`Living Entity`](#living-entity)

| Name | Description | Type of the value | Input Example |
|---|---|---|---|
| `player/main_hand` | Main Hand of the disguise | Hand, `left` or `right` | `left`, `right` |
| `player/skin` | Skin of the disguise | [GameProfile](https://minecraft.wiki/w/Data_component_format#profile) | Well... There used to be an example input, but later I figured out that Minecraft seems no longer has relevant codecs, so... 🙃 |

### Copper Golem

| Name | Description | Type of the value | Input Example |
|---|---|---|---|
| `copper_golem/weather_state` | The Weather State of the Copper Golem | A WeatherState, can be any from the Input Example | `unaffected`, `exposed`, `weathered`, `oxidized` |