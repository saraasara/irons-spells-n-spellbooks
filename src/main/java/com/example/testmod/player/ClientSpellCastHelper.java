package com.example.testmod.player;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.ender.TeleportSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;

public class ClientSpellCastHelper {
    /**
     * Network Handling Wrapper
     */
    public static void handleClientboundOnClientCast(int spellId, int level, CastSource castSource) {
        var spell = AbstractSpell.getSpell(spellId, level);
        spell.onClientCastComplete(Minecraft.getInstance().player.level, Minecraft.getInstance().player, null);
    }

    public static void handleClientboundTeleport(Vec3 pos1, Vec3 pos2) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            TeleportSpell.particleCloud(level, player, pos1);
            TeleportSpell.particleCloud(level, player, pos2);
        }
    }

    public static void doTargetHealParticles(Vec3 pos) {
        //Copied from arrow because these particles use their motion for color??
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            int i = PotionUtils.getColor(Potion.byName("healing"));
            double d0 = (double) (i >> 16 & 255) / 255.0D;
            double d1 = (double) (i >> 8 & 255) / 255.0D;
            double d2 = (double) (i >> 0 & 255) / 255.0D;

            for (int j = 0; j < 15; ++j) {
                level.addParticle(ParticleTypes.ENTITY_EFFECT, pos.x + getRandomScaled(0.25D), pos.y + getRandomScaled(1), pos.z + getRandomScaled(0.25D), d0, d1, d2);
            }
        }

    }

    private static double getRandomScaled(double scale) {
        return (2.0D * Math.random() - 1.0D) * scale;
    }
}
