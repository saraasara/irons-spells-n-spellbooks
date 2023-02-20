package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.wither_skull.WitherSkullProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class WitherSkullSpell extends AbstractSpell {
    public WitherSkullSpell() {
        this(1);
    }

    public WitherSkullSpell(int level) {
        super(SpellType.WITHER_SKULL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getDamage(null), 1)));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.WITHER_SHOOT);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        float speed = (8 + this.level) * .01f;
        float damage = getDamage(entity);
        WitherSkullProjectile skull = new WitherSkullProjectile(entity, level, speed, damage);
        Vec3 spawn = entity.getEyePosition().add(entity.getForward());
        skull.moveTo(spawn.x, spawn.y - skull.getBoundingBox().getYsize() / 2, spawn.z, entity.getYRot() + 180, entity.getXRot());
        level.addFreshEntity(skull);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return this.getSpellPower(entity);
    }
}
