package net.bagaja.tridentmod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;

@Mod("tridentmod")
public class TridentMod {
    public static final String MOD_ID = "tridentmod";

    public TridentMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ThrownTrident) {
            ThrownTrident trident = (ThrownTrident) event.getEntity();
            // Set thrown damage to match Bedrock (9 damage)
            trident.setBaseDamage(9.0D);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getDirectEntity();
        if (source instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            if (weapon.getItem() instanceof TridentItem) {
                // Modify melee damage to match Bedrock (11 damage)
                float newDamage = 11.0F;
                event.setAmount(newDamage);
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Level level = event.level;
            for (Entity entity : level.players()) {
                if (entity instanceof Player player) {
                    level.getEntitiesOfClass(ThrownTrident.class, player.getBoundingBox().inflate(32.0D)).forEach(trident -> {
                        if (hasLoyalty(trident) && isReturning(trident)) {
                            double speedMultiplier = 1.5D;
                            trident.setDeltaMovement(trident.getDeltaMovement().multiply(speedMultiplier, speedMultiplier, speedMultiplier));
                        }
                    });
                }
            }
        }
    }

    private boolean hasLoyalty(ThrownTrident trident) {
        CompoundTag tag = trident.saveWithoutId(new CompoundTag());
        if (tag.contains("Trident")) {
            CompoundTag tridentTag = tag.getCompound("Trident");
            return tridentTag.contains("Enchantments") &&
                    tridentTag.getList("Enchantments", 10).toString().contains("minecraft:loyalty");
        }
        return false;
    }

    private boolean isReturning(ThrownTrident trident) {
        return trident.getDeltaMovement().length() > 0 && // Is moving
                trident.getOwner() instanceof LivingEntity && // Has an owner
                trident.isNoPhysics(); // This indicates the trident is in return mode
    } }
