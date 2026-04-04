package com.pekar.callofcompanions.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class LivingEntityEvents implements IEventHandler
{
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event)
    {
    }
}
