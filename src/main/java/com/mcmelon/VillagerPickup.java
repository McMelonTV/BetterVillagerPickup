package com.mcmelon;

import com.mcmelon.Events.RightClickEventListener;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerPickup implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("villagerpickup");

	@Override
	public void onInitialize() {
		RightClickEventListener.registerRightClickEvent();
	}
}