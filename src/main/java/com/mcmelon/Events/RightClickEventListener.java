package com.mcmelon.Events;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class RightClickEventListener {
	public static void registerRightClickEvent() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (hand != Hand.MAIN_HAND) {
				return ActionResult.PASS;
			}

			boolean isHoldingVillagerEgg = player.getStackInHand(hand).getItem() == SpawnEggItem.forEntity(EntityType.VILLAGER);

			// This is to prevent a weird bug where a baby villager without the NBT data is
			// spawned when a player right clicks a villager with a villager spawn egg
			if (entity instanceof VillagerEntity && isHoldingVillagerEgg && !player.isSneaking()) {
				player.sendMessage(Text.of("§cCannot spawn a villager into another villager"), true);
				return ActionResult.FAIL;
			}

			if (player.isSneaking() && entity instanceof VillagerEntity) {
				VillagerEntity villager = (VillagerEntity) entity;
				NbtCompound villagerNbt = new NbtCompound();
				villager.writeCustomDataToNbt(villagerNbt);
				villagerNbt.putString("id", "");
				NbtComponent nbtc = NbtComponent.of(villagerNbt);

				Item spawnEgg = SpawnEggItem.forEntity(villager.getType());
				if (spawnEgg != null) {
					ItemStack spawnEggStack = new ItemStack(spawnEgg);

					String name = "Villager";

					boolean isBaby = villager.isBaby();
					name = isBaby ? "Baby " + name : name;

					String profession = villager.getVillagerData().getProfession().toString();
					profession = profession.substring(0, 1).toUpperCase() + profession.substring(1);
					name = profession.contains("None") ? name : profession + " " + name;

					String biome = villager.getVillagerData().getType().toString();
					biome = biome.substring(0, 1).toUpperCase() + biome.substring(1);
					name = biome + " " + name;

					float hl = villager.getHealth();
					BigDecimal hlbd = new BigDecimal(hl).setScale(1, RoundingMode.UNNECESSARY);
                    String health = hlbd + " / " + villager.getMaxHealth() + " ❤";

					List<String> tradesList = new java.util.ArrayList<>(List.of());
					NbtList tradeList = villagerNbt.getCompound("Offers").getList("Recipes", 10);

					/*
	Offers: {
		Recipes: [
			{
				maxUses: 16,
				sell: {
					count: 1,
					id: "minecraft:emerald"
				},
				buy: {
					count: 10,
					id: "minecraft:coal"
				},
				xp: 2,
				priceMultiplier: 0.05f
			},
			{
				maxUses: 16,
				sell: {
					count: 1,
					id: "minecraft:cod_bucket"
				},
				buy: {
					count: 3,
					id: "minecraft:emerald"
				},
				priceMultiplier: 0.05f
			}
		]
	},
					*/

					for (int i = 0; i < tradeList.size(); i++) {
						NbtCompound trade = tradeList.getCompound(i);
						NbtCompound buy = trade.getCompound("buy");
						NbtCompound buyB = trade.contains("buyB") ? trade.getCompound("buyB") : null;
						NbtCompound sell = trade.getCompound("sell");

						int buyCount = buy.getInt("count");
						String buyId = buy.getString("id");
						String buyItemName = Registries.ITEM.get(Identifier.of(buyId)).getName().getString();

						if (buyB != null) {
							int buyBCount = buyB.getInt("count");
							String buyBId = buyB.getString("id");
							String buyBItemName = Registries.ITEM.get(Identifier.of(buyBId)).getName().getString();
							buyItemName += " + " + buyBCount + "x " + buyBItemName;
						}

						int sellCount = sell.getInt("count");
						String sellId = sell.getString("id");
						String sellItemName = Registries.ITEM.get(Identifier.of(sellId)).getName().getString();

						String tradeString = buyCount + "x " + buyItemName + " -> " + sellCount + "x " + sellItemName;
						tradesList.add(tradeString);
					}

					String workstationLocation = "";
					int[] workstationLoc = villagerNbt.getCompound("Brain").getCompound("memories").getCompound("minecraft:job_site").getCompound("value").getIntArray("pos");
					String workstationDim = villagerNbt.getCompound("Brain").getCompound("memories").getCompound("minecraft:job_site").getCompound("value").getString("dimension");
					if (workstationLoc.length != 0) {
						String dim = workstationDim.split(":")[1];
						dim = dim.substring(0, 1).toUpperCase() + dim.substring(1);
						workstationLocation += workstationLoc[0] + ", " + workstationLoc[1] + ", " + workstationLoc[2] + " (" + dim + ")";
					} else {
						workstationLocation += "None";
					}

					String bedLocation = "";
					int[] bedLoc = villagerNbt.getCompound("Brain").getCompound("memories").getCompound("minecraft:home").getCompound("value").getIntArray("pos");
					String bedDim = villagerNbt.getCompound("Brain").getCompound("memories").getCompound("minecraft:home").getCompound("value").getString("dimension");
					if (bedLoc.length != 0) {
						String dim = bedDim.split(":")[1];
						dim = dim.substring(0, 1).toUpperCase() + dim.substring(1);
						bedLocation += bedLoc[0] + ", " + bedLoc[1] + ", " + bedLoc[2] + " (" + dim + ")";
					} else {
						bedLocation += "None";
					}

					Style style = Style.EMPTY.withItalic(false).withColor(Colors.GRAY);

					List<Text> loreLines = new java.util.ArrayList<>(List.of(
                            Text.literal("Profession: " + profession).setStyle(style),
                            Text.literal("Biome Type: " + biome).setStyle(style),
                            Text.literal("Baby: " + isBaby).setStyle(style),
                            Text.literal(""),
                            Text.literal("Health: " + health).setStyle(style),
                            Text.literal("Workstation: " + workstationLocation).setStyle(style),
                            Text.literal("Bed: " + bedLocation).setStyle(style)
                    ));

					if (!tradesList.isEmpty()) {
						loreLines.add(Text.literal(""));
						loreLines.add(Text.literal("Trades: ").setStyle(style));
						for (String trade : tradesList) {
							loreLines.add(Text.literal("  " + trade).setStyle(style));
						}
					}

					spawnEggStack.set(DataComponentTypes.ITEM_NAME, Text.of(name));
					spawnEggStack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
					spawnEggStack.set(DataComponentTypes.ENTITY_DATA, nbtc);

					if (player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
						player.setStackInHand(Hand.MAIN_HAND, spawnEggStack);
					} else {
						boolean success = player.giveItemStack(spawnEggStack);
						if (!success) {
							return ActionResult.FAIL;
						}
					}
				}

				villager.remove(RemovalReason.DISCARDED);
				return ActionResult.SUCCESS;
			}

			return ActionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
			boolean isSpawner = world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.SPAWNER;
			boolean isVillagerEgg = player.getStackInHand(hand).getItem() == SpawnEggItem.forEntity(EntityType.VILLAGER);

			if (isSpawner && isVillagerEgg) {
				player.sendMessage(Text.of("§cCannot use villager spawn egg on spawner"), true);
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});
	}
}