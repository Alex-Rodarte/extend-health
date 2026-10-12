
package com.finity4;

import com.mojang.serialization.Codec;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendHealth implements ModInitializer {

	public static final String MOD_ID = "extend-health";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	// Guarda los corazones adicionales de forma persistente.
	public static final AttachmentType<Integer> EXTRA_HEARTS =
			AttachmentRegistry.create(
					id("extra_hearts"),
					builder -> builder
							.persistent(Codec.INT)
							.initializer(() -> 0)
							.copyOnDeath()
			);

	// Registra el Corazón Vital.
	public static final Item VITAL_HEART = registerVitalHeart();

	private static Item registerVitalHeart() {
		ResourceKey<Item> key = ResourceKey.create(
				Registries.ITEM,
				id("vital_heart")
		);

		return Registry.register(
				BuiltInRegistries.ITEM,
				key,
				new VitalHeartItem(
						new Item.Properties().setId(key)
				)
		);
	}

	// Identificador de la pestaña del modo Creativo.
	public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY =
			ResourceKey.create(
					Registries.CREATIVE_MODE_TAB,
					id("creative_tab")
			);

	// Calcula la vida máxima a partir de los corazones guardados.
	public static void applyExtraHealth(ServerPlayer player) {
		int hearts = player.getAttachedOrCreate(EXTRA_HEARTS);

		var healthAttribute =
				player.getAttribute(
						net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH
				);

		if (healthAttribute != null) {
			healthAttribute.setBaseValue(20.0 + hearts * 2.0);
		}
	}

	@Override
	public void onInitialize() {

		// Registra la pestaña personalizada del modo Creativo.
		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				CREATIVE_TAB_KEY,
				CreativeModeTab.builder(CreativeModeTab.Row.TOP,0)
						.title(Component.translatable(
								"creativetab.extend-health"
						))
						.icon(() -> new ItemStack(VITAL_HEART))
						.displayItems((parameters, output) -> {
							output.accept(VITAL_HEART);
						})
						.build()
		);

		// Restaura la vida máxima cuando el jugador entra al mundo.
		ServerPlayerEvents.JOIN.register(
				ExtendHealth::applyExtraHealth
		);

		// Conserva los corazones al morir y reaparecer.
		ServerPlayerEvents.COPY_FROM.register(
				(oldPlayer, newPlayer, alive) -> {
					int hearts =
							oldPlayer.getAttachedOrCreate(EXTRA_HEARTS);

					newPlayer.setAttached(EXTRA_HEARTS, hearts);

					applyExtraHealth(newPlayer);

					LOGGER.info(
							"Corazones adicionales restaurados: {}",
							hearts
					);
				}
		);

		LOGGER.info("Extend Health iniciado correctamente");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
