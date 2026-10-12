
package com.finity4;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class VitalHeartItem extends Item {

    public VitalHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        // Los cambios de vida se realizan únicamente en el servidor.
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {

            // Incrementa el contador persistente.
            int hearts = serverPlayer.getAttachedOrCreate(
                    ExtendHealth.EXTRA_HEARTS
            );

            hearts++;

            serverPlayer.setAttached(
                    ExtendHealth.EXTRA_HEARTS,
                    hearts
            );

            // Actualiza la vida máxima.
            AttributeInstance healthAttribute =
                    serverPlayer.getAttribute(Attributes.MAX_HEALTH);

            if (healthAttribute != null) {
                healthAttribute.setBaseValue(20.0 + hearts * 2.0);
            }

            // Recupera dos puntos de vida.
            serverPlayer.setHealth(
                    Math.min(
                            serverPlayer.getHealth() + 2.0F,
                            serverPlayer.getMaxHealth()
                    )
            );

            // Consume un corazón del inventario.
            serverPlayer.getItemInHand(hand).shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
