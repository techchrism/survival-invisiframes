# Survival Invisiframes

This plugin enables the use of 1.16's invisible item frames for survival players

Invisible item frames are crafted similar to tipped arrows - one lingering invisibility potion surrounded by 8 item frames\
![Recipe Screenshot](https://i.imgur.com/RtX84ic.png)

In 1.17+, an invisible item frame can be crafted with a glow ink sac to create a glowing invisible item frame

## Permissions
Permission | Description
--- | ---
`survivalinvisiframes.place` | Allows the player to place an invisible item frame (enabled by default)
`survivalinvisiframes.craft`| Allows the player to craft an invisible item frame (enabled by default)
`survivalinvisiframes.cmd` | Allows the player to run commands from this plugin
`survivalinvisiframes.reload` | Permission to run `/iframe reload`
`survivalinvisiframes.forcerecheck` | Permission to run `/iframe force-recheck`
`survivalinvisiframes.get` | Permission to run `/iframe get`
`survivalinvisiframes.setitem` | Permission to run `/iframe additem` and `/iframe removeitem`

## Commands
Permission required for all commands: `survivalinvisiframes.cmd`

Command | Description | Permission
--- | --- | ---
`/iframe` or `/iframe get` | Gives the player an invisible item frame | `survivalinvisiframes.get`
`/iframe reload` | Reloads the config | `survivalinvisiframes.reload`
`/iframe force-recheck` | Rechecks all loaded invisible item frames to add/remove slimes manually | `survivalinvisiframes.forcerecheck`
`/iframe additem` | Adds the held item to the list of recipe center items | `survivalinvisiframes.setitem`
`/iframe removeitem` | Removes the held item from the list of recipe center items | `survivalinvisiframes.setitem`

## Config
```yaml
# Whether or not to enable invisible item frames glowing when there's no item in them
# This will also make them visible when there's no item in them
item-frames-glow: true

# The item in the center of the recipe
# Recommended to use "/iframe additem" and "/iframe removeitem" to change this
recipe-center-items:
  - ==: org.bukkit.inventory.ItemStack
    v: 2567
    type: LINGERING_POTION
    meta:
      ==: ItemMeta
      meta-type: POTION
      potion-type: minecraft:invisibility
  - ==: org.bukkit.inventory.ItemStack
    v: 2567
    type: LINGERING_POTION
    meta:
      ==: ItemMeta
      meta-type: POTION
      potion-type: minecraft:long_invisibility
```