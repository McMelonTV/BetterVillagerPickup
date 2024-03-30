package com.mcmelon.Events;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;

public class RightClickEventListener {
	public static void registerRightClickEvent() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (hand != Hand.MAIN_HAND) {
				return ActionResult.PASS;
			}

			boolean isHoldingVillagerEgg = player.getStackInHand(hand).getItem() == SpawnEggItem
					.forEntity(EntityType.VILLAGER);

			if (entity instanceof VillagerEntity && isHoldingVillagerEgg && !player.isSneaking()) {
				player.sendMessage(Text.of("§cCannot spawn a villager into another villager"), true);
				return ActionResult.FAIL;
			}

			if (player.isSneaking() && entity instanceof VillagerEntity) {
				VillagerEntity villager = (VillagerEntity) entity;
				NbtCompound nbt = new NbtCompound();
				villager.writeCustomDataToNbt(nbt);

				Item spawnEgg = SpawnEggItem.forEntity(villager.getType());
				if (spawnEgg != null) {
					ItemStack spawnEggStack = new ItemStack(spawnEgg);
					String profession = villager.getVillagerData().getProfession().toString();
					profession = profession.substring(0, 1).toUpperCase() + profession.substring(1);
					String professionString = profession.contains("None") ? "" : profession + " ";

					NbtCompound nbtCompound = new NbtCompound();
					nbtCompound.put("EntityTag", nbt);

					NbtCompound display = new NbtCompound();
					display.putString(ItemStack.NAME_KEY, "{\"text\":\"" + professionString + "Villager\",\"color\":\"yellow\",\"italic\":false}");

					NbtList lore = new NbtList();
					lore.add(NbtString.of("{\"text\":\"§7Contains Villager Data\",\"color\":\"gray\"}"));
					display.put(ItemStack.LORE_KEY, lore);

					nbtCompound.put(ItemStack.DISPLAY_KEY, display);

					spawnEggStack.setNbt(nbtCompound);

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
			boolean isVillagerEgg = player.getStackInHand(hand).getItem() == SpawnEggItem
					.forEntity(EntityType.VILLAGER);

			if (isSpawner && isVillagerEgg) {
				player.sendMessage(Text.of("§cCannot use villager spawn egg on spawner"), true);
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});
	}
}