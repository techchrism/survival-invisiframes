# Survival Invisiframes

This plugin enables the use of 1.16's invisible item frames for survival players

Invisible item frames are crafted similar to tipped arrows - one lingering invisibility potion surrounded by 8 item frames\
![Recipe Screenshot](https://i.imgur.com/RtX84ic.png)

## Permissions
 - survivalinvisiframes.place\
   Allows the player to place an invisible item frame (enabled by default)
 - survivalinvisiframes.craft\
   Allows the player to craft an invisible item frame (enabled by default)
 - survivalinvisiframes.cmd\
   Allows the player to run commands from this plugin
 - survivalinvisiframes.reload\
   Permission to run /iframe reload
 - survivalinvisiframes.forcerecheck\
   Permission to run /iframe force-recheck
 - survivalinvisiframes.get\
   Permission to run /iframe get

## Commands
Permission required for all commands: survivalinvisiframes.cmd
 - /iframe or /iframe get\
   Gives the player an invisible item frame\
   Permission: survivalinvisiframes.get
 - /iframe reload\
   Reloads the config\
   Permission: survivalinvisiframes.reload
 - /iframe force-recheck\
   Rechecks all loaded invisible item frames to add/remove slimes manually\
   Permission: survivalinvisiframes.forcerecheck

## Config
```yaml
# Whether or not to enable invisible slimes for easily seeing invisible item frames
slimes-enabled: true
```