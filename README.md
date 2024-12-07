# Better Villager Pickup
A simple **server-side** mod that allows you to pick up villagers.
Since this is a server-side mod, it works with vanilla clients.

This is a fork of [Villager Pickup](https://modrinth.com/mod/villager-pickup) which implements a fix to prevent players from putting picked up villagers into mob spawners or spawning them inside other villagers (by default this would spawn a baby villager with no data).
This fork also adds a few additions like showing the villager's profession, type, health, workstation and bed locations and trades in the item tooltip.

To pick up a villager, simply sneak and right click on them.

The villager spawn egg will retain all NBT data of the villager meaning it will stay the same way you picked it up. (keep xp, trades, etc)

### Planned Features
- Add a config option to change which entities can be picked up
