
package com.finity4;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class cursedheartItem extends Item {

    public cursedheartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer) {

            int hearts = serverPlayer.getAttachedOrCreate(
                    ExtendHealth.EXTRA_HEARTS
            );

            // Solo quita corazones adicionales.
            if (hearts > 0) {
                hearts--;

                serverPlayer.setAttached(
                        ExtendHealth.EXTRA_HEARTS,
                        hearts
                );

                AttributeInstance healthAttribute =
                        serverPlayer.getAttribute(
                                Attributes.MAX_HEALTH
                        );

                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(
                            20.0 + hearts * 2.0
                    );
                }

                // Evita que la vida actual supere la nueva vida máxima.
                serverPlayer.setHealth(
                        Math.min(
                                serverPlayer.getHealth(),
                                serverPlayer.getMaxHealth()
                        )
                );

                // Consume el objeto solo si quitó un corazón.
                serverPlayer.getItemInHand(hand).shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
