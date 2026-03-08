*The actual behavior for permissions with an `unset` default depends on the platform you use. For most situations, it should be `op`*

### Morphing
| Node                                                | Description                                                                                                                                                                                                                                                                  | Default                                        |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------|
| feathermorph.morph                                  | Permission for players to morph themselves                                                                                                                                                                                                                                   | true                                           |
| feathermorph.acquire_morph                          | Whether the player could acquire disguises from gameplay features                                                                                                                                                                                                            | true                                           |
| feathermorph.morph.as.*[minecraft \| player].[...]* | Permission for player to morph into specific mob or player. <br> For example, the node for morphing into sheep (`minecraft:sheep`) is **`feathermorph.morph.as.minecraft.sheep`**, and the node for morphing into player *Notch* is **`feathermorph.morph.as.player.Notch`** | *unset, `true` by default*                     |
| feathermorph.unmorph                                | Permission for players to unmorph themselves                                                                                                                                                                                                                                 | true                                           |
| feathermorph.headmorph                              | Allows morphing into mobs or players using heads                                                                                                                                                                                                                             | true                                           |
| feathermorph.skill                                  | Allows activating skills                                                                                                                                                                                                                                                     | true                                           |
| feathermorph.chatoverride                           | Allows using ChatOverride                                                                                                                                                                                                                                                    | true                                           |
| feathermorph.mirror                                 | Allows activating InteractionMirror                                                                                                                                                                                                                                          | true                                           |
| feathermorph.mirror.immune                          | Make a player immune from InteractionMirror                                                                                                                                                                                                                                  | *unset, `false` by default; `false` in 1.0.10* |
| feathermorph.mirror.mannequin                       | Players with this permission are also able to mirror some operation to nearby mannequins                                                                                                                                                                                     | false                                          |
| feathermorph.request.*<accept \| deny \| send>*     | Allows *accept/deny/send* exchange requests                                                                                                                                                                                                                                  | true                                           |
| ~~feathermorph.disguise_use_real_uuid~~             | (Removed) Whether player's disguise would use player's UUID for the virtual entity. Note that changing this permission doesn't affect players who is already disguising                                                                                                            | false                                          |
| feathermorph.feathermorph.disguise_properties.use   | Whether a player can use disguise properties to customize their disguise                                                                                                                                                                                                     | true                                           |

### Disguise Properties
| Node                                                  | Description                                                                         | Default                                        |
|-------------------------------------------------------|-------------------------------------------------------------------------------------|------------------------------------------------|
| feathermorph.custom_skin                              | Whether a player can customize their skin for Mannequin/Player disguise             | op                                             |
| feathermorph.custom_text                              | Whether a player can customize texts for their disguise                             | true                                           |
| feathermorph.disguise_properties.use                  | Whether a player can customize their disguise using disguise properties             | true                                           |                
| feathermorph.disguise_properties.custom_skin_on_items | Whether a player can customize skin profile for items in their disguise's equipment | op                                             |

### Managing
| Node                            | Description                                                                                           | Default |
|---------------------------------|-------------------------------------------------------------------------------------------------------|---------|
| feathermorph.admin              | When AI Modification is changed, a warning message will be send to all players having this permission | op      |
| feathermorph.manage             | Allows usage for `/fm manage` command                                                                 | op      |
| feathermorph.query              | Allows checking disguise state for a player                                                           | op      |
| feathermorph.queryall           | Allows listing all disguised players                                                                  | op      |
| feathermorph.reload             | Allows reloading the configuration                                                                    | op      |
| feathermorph.stat               | Allows usage for `/fm stat` command                                                                   | op      |
| feathermorph.toggle             | Allows changing settings using the `/fm option` command                                               | op      |
| feathermorph.lookup             | Allow to check a player's unlocked disguises                                                          | op      |
| feathermorph.skin_cache         | Allow access to the `/fm skin_cache` command                                                          | op      |
| feathermorph.make_disguise_tool | Whether a player can use the `/fm make_disguise_tool` command                                         | op      |

### Other
| Node                              | Description                                                              | Default                    |
|-----------------------------------|--------------------------------------------------------------------------|----------------------------|
| feathermorph.disguise_revealing   | Whether a player can see disguises' holder name with Client Integration  | unset; `op` in 1.0.10      |
| feathermorph.can_fly              | Whether a player can use the fly ability                                 | true                       |
| feathermorph.can_fly.always       | Whether a player can always use the fly ability, ignoring the conditions | unset                      |
| feathermorph.can_fly.in.\<world\> | Whether a player can use the fly ability in a world                      | *unset, `true` by default* |
| feathermorph.magic_bottle.use     | Whether a player can use the Magic Bottle feature                        | true                       |
| feathermorph.magic_bottle.exclude | Whether a player's disguise cannot be collected using a Magic Bottle     | false                      |

### Deprecated
| Node                         | Description                                                                        | Default |
|------------------------------|------------------------------------------------------------------------------------|---------|
| feathermorph.toggle_town_fly | Whether a player can toggle if their town supports morph flight                    | true    |
| feathermorph.switch_backend  | Allow switching current disguise backend (Backend switching available for use yet) | op      |