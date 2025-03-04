package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.ISpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.EchoingStrikeEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class EchoingStrikesEffect extends MagicMobEffect {
    public EchoingStrikesEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @SubscribeEvent
    public static void createEcho(LivingHurtEvent event) {
        var damageSource = event.getSource();
        if (!(damageSource instanceof ISpellDamageSource) && damageSource.getEntity() instanceof LivingEntity attacker) {
            var effect = attacker.getEffect(MobEffectRegistry.ECHOING_STRIKES.get());
            if (effect != null) {
                var percent = getDamageModifier(effect.getAmplifier(), attacker);
                EchoingStrikeEntity echo = new EchoingStrikeEntity(attacker.level, attacker, event.getAmount() * percent, 2f);
                echo.setPos(event.getEntity().position());
                attacker.level.addFreshEntity(echo);
            }
        }
    }

    public static float getDamageModifier(int effectAmplifier, @Nullable LivingEntity caster) {
        var power = caster == null ? 1 : SpellRegistry.ECHOING_STRIKES_SPELL.get().getEntityPowerMultiplier(caster);
        return (((effectAmplifier - 4) * power) + 5) * .1f; // create echo of 10% damage per level of the effect
    }
}
