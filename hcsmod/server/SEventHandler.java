/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.guns.EntityDamageSourceGun
 *  hcsmod.HCS
 *  hcsmod.clans.common.ClanBase$State
 *  hcsmod.clans.common.ClanPlayer
 *  hcsmod.clans.common.ClanPlayer$Role
 *  hcsmod.cunches.IVehicle
 *  hcsmod.effects.DamageType
 *  hcsmod.effects.Effect
 *  hcsmod.effects.EnactEffect
 *  hcsmod.entity.EntityCorpse
 *  hcsmod.entity.EntitySnowmanHCS
 *  hcsmod.entity.EntityZombieDayZ
 *  hcsmod.player.ContainerExtended
 *  hcsmod.player.ExtendedPlayer
 *  hcsplatfom.PlatformBridge
 *  mcheli.aircraft.MCH_EntityAircraft
 *  mcheli.throwable.MCH_EntityThrowable
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.item.EntityXPOrb
 *  net.minecraft.entity.monster.EntityCreeper
 *  net.minecraft.entity.monster.EntityEnderman
 *  net.minecraft.entity.monster.EntitySkeleton
 *  net.minecraft.entity.monster.EntitySlime
 *  net.minecraft.entity.monster.EntitySpider
 *  net.minecraft.entity.monster.EntityZombie
 *  net.minecraft.entity.passive.EntityOcelot
 *  net.minecraft.entity.passive.EntitySheep
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.entity.projectile.EntityFishHook
 *  net.minecraft.item.ItemStack
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatMessageComponent
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.Vec3
 *  net.minecraftforge.event.Event$Result
 *  net.minecraftforge.event.EventPriority
 *  net.minecraftforge.event.ForgeSubscribe
 *  net.minecraftforge.event.entity.EntityEvent$EntityConstructing
 *  net.minecraftforge.event.entity.EntityJoinWorldEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingUpdateEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.living.LivingSpawnEvent$CheckSpawn
 *  net.minecraftforge.event.entity.player.PlayerDropsEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$Action
 *  vintarz.core.VSP
 */
package hcsmod.server;

import co.uk.flansmods.common.guns.EntityDamageSourceGun;
import hcsmod.HCS;
import hcsmod.clans.common.ClanBase;
import hcsmod.clans.common.ClanPlayer;
import hcsmod.clans.server.ClansServer;
import hcsmod.clans.server.ServerBase;
import hcsmod.cunches.IVehicle;
import hcsmod.effects.DamageType;
import hcsmod.effects.Effect;
import hcsmod.effects.EnactEffect;
import hcsmod.entity.EntityCorpse;
import hcsmod.entity.EntitySnowmanHCS;
import hcsmod.entity.EntityZombieDayZ;
import hcsmod.player.ContainerExtended;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.EntityHouseServer;
import hcsmod.server.ExtendedStorage;
import hcsmod.server.HcsServer;
import hcsmod.server.RandomSpawn;
import hcsmod.server.SPacketHandler;
import hcsmod.server.SPlayerTracker;
import hcsmod.server.zones.PveSystem;
import hcsplatfom.PlatformBridge;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.throwable.MCH_EntityThrowable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import vintarz.core.VSP;
import vintarz.ntr.server.NtrPlayerData;
import vintarz.ntr.server.ServerNtr;

public class SEventHandler {
    static Random rand = new Random();
    private static final ChatMessageComponent cheater_kill = ChatMessageComponent.createFromText((String)"\u0427\u0438\u0442\u0435\u0440 \u0443\u043c\u0435\u0440 \u043e\u0442 \u043e\u0434\u043d\u043e\u0433\u043e \u0432\u044b\u0441\u0442\u0440\u0435\u043b\u0430/\u0443\u0434\u0430\u0440\u0430. \u0422\u0435\u043f\u0435\u0440\u044c \u043c\u043e\u0436\u0435\u0448\u044c \u043d\u0430\u0439\u0442\u0438 \u0435\u0433\u043e \u0434\u043e\u043c\u0430 \u0438 \u043e\u0433\u0440\u0430\u0431\u0438\u0442\u044c.");

    @ForgeSubscribe
    public void onDamage(LivingAttackEvent e) {
        EntityPlayerMP attacker = null;
        ExtendedPlayer attackerExt = null;
        ExtendedStorage attackerStorage = null;
        if (e.source.getSourceOfDamage() instanceof EntityPlayerMP) {
            attacker = (EntityPlayerMP)e.source.getSourceOfDamage();
            attackerExt = ExtendedPlayer.server((EntityPlayer)attacker);
            attackerStorage = ExtendedStorage.get(attackerExt);
            attackerExt.logoutTime = 0L;
        }
        e.entity.hurtResistantTime = 0;
        if (e.entity instanceof EntityPlayerMP) {
            if (e.entity.posX * e.entity.posX + (e.entity.posY - 1.0) * (e.entity.posY - 1.0) + e.entity.posZ * e.entity.posZ <= 25.0) {
                e.setCanceled(true);
                return;
            }
            if (e.entity.ridingEntity instanceof EntityHouseServer) {
                e.setCanceled(true);
                return;
            }
            EntityPlayerMP target = (EntityPlayerMP)e.entity;
            ExtendedPlayer targetExt = ExtendedPlayer.server((EntityPlayer)target);
            ExtendedStorage targetStorage = ExtendedStorage.get(targetExt);
            if (e.source != DamageSource.fall && HcsServer.pveSystem.isImmune(targetStorage.pvePlayer)) {
                e.setCanceled(true);
                if (attacker != null) {
                    SPacketHandler.sendHint((EntityPlayer)attacker, "pve-tgt", "\u00a7c\u0418\u0433\u0440\u043e\u043a \u043d\u0435\u0443\u044f\u0437\u0432\u0438\u043c \u043a \u0430\u0442\u0430\u043a\u0430\u043c " + ((targetStorage.pvePlayer.immuneTimeout() - System.currentTimeMillis()) / 1000L + 1L) + "\u0441", 60);
                }
                return;
            }
            if (targetExt.shieldCharge > 0) {
                if (targetExt.shieldCharge == 600) {
                    targetExt.shieldCharge = -20;
                    targetExt.shieldTimeout = System.currentTimeMillis() + 1000L;
                } else {
                    targetExt.shieldCharge = 0;
                }
            }
            if (targetExt.shieldCharge < 0) {
                if (System.currentTimeMillis() <= targetExt.shieldTimeout) {
                    e.setCanceled(true);
                    return;
                }
                targetExt.shieldCharge = 0;
            }
            if (attacker != null) {
                if (attacker.o instanceof EntityHouseServer) {
                    e.setCanceled(true);
                    return;
                }
                attackerExt.housePvpTimeout = targetExt.housePvpTimeout = System.currentTimeMillis() + 15000L;
                attackerStorage.lastPVPtime = targetStorage.lastPVPtime = System.currentTimeMillis();
                PveSystem.Player pveAttacker = attackerStorage.pvePlayer;
                if (!HcsServer.pveSystem.ensureCanPvPAttack((EntityPlayer)attacker, pveAttacker)) {
                    e.setCanceled(true);
                    return;
                }
                NtrPlayerData ntr = ServerNtr.get(target.c_());
                if (ntr != null && ntr.allies.contains(attacker.c_())) {
                    if (HcsServer.isLiteserver) {
                        e.setCanceled(true);
                    } else {
                        EntityPlayer player = (EntityPlayer)e.entity;
                        SPacketHandler.sendHint((EntityPlayer)attacker, "frndfire", "\u00a7a\u0412\u044b \u043d\u0430\u043d\u0435\u0441\u043b\u0438 \u0443\u0440\u043e\u043d \u0434\u0440\u0443\u0433\u0443 " + player.username, 40);
                        SPacketHandler.sendHint(player, "frndfire", "\u00a7a\u0412\u0430\u043c \u043d\u0430\u043d\u0435\u0441 \u0443\u0440\u043e\u043d \u0434\u0440\u0443\u0433 " + attacker.bu, 40);
                    }
                }
            }
        }
        if (e.entity instanceof EntityPlayer && e.source instanceof EntityDamageSourceGun && e.source.getSourceOfDamage() instanceof EntityLivingBase) {
            EntityPlayer entityPlayer = (EntityPlayer)e.entity;
            EntityLivingBase rangeAttacker = (EntityLivingBase)e.source.getSourceOfDamage();
            double dx = entityPlayer.u - rangeAttacker.u;
            double dz = entityPlayer.w - rangeAttacker.w;
            float attackAngel = (float)Math.abs((Math.toDegrees(Math.atan2(dx, dz)) - 180.0) % 360.0);
            SPacketHandler.sendDamageAngle(entityPlayer, attackAngel, e.ammount);
        }
    }

    @ForgeSubscribe
    public void disableRegen(EntityJoinWorldEvent ev) {
        if (ev.world.getGameRules().getGameRuleBooleanValue("naturalRegeneration")) {
            ev.world.getGameRules().setOrCreateGameRule("naturalRegeneration", "false");
        }
    }

    @ForgeSubscribe
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            ExtendedPlayer.server((EntityPlayer)((EntityPlayer)event.entity));
        }
    }

    @ForgeSubscribe
    public void onDamageTaken(LivingDeathEvent ev) {
        ev.entity.mountEntity(null);
    }

    @ForgeSubscribe
    public void onDamageTaken(LivingHurtEvent ev) {
        if (ev.entityLiving instanceof EntityPlayerMP && ev.source.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP attacker = (EntityPlayerMP)ev.source.getEntity();
            HashSet data = (HashSet)PlatformBridge.PLAYER_FLAGS.get(attacker.bu);
            if (data != null && (data.contains("nodmg") || data.contains("cheater"))) {
                if (!HcsServer.playerHasData(((EntityPlayerMP)ev.entityLiving).bu, "cheater")) {
                    ev.setCanceled(true);
                }
                return;
            }
        }
        if (ev.source == DamageSource.fall && ev.entityLiving.getCurrentItemOrArmor(1) != null && ev.entityLiving.getCurrentItemOrArmor((int)1).itemID == HCS.JAG[3].cv) {
            ev.ammount = 2.0f;
        }
        if (ev.source == DamageSource.fall && SEventHandler.isProtaSet(ev.entityLiving)) {
            ev.ammount = 2.0f;
        }
        if (ev.source == DamageSource.fall && ev.ammount > 2.0f) {
            ev.entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 6000, 1));
        }
        if (ev.entityLiving instanceof EntityPlayer && (ev.ammount >= 10.0f || ev.ammount >= 5.0f && rand.nextBoolean()) && (ev.entityLiving.getCurrentItemOrArmor(3) == null || ev.entityLiving.getCurrentItemOrArmor((int)3).itemID != HCS.JAG[1].cv) && !SEventHandler.isProtaSet(ev.entityLiving)) {
            ev.entityLiving.addPotionEffect((PotionEffect)new EnactEffect(Effect.bleeding.c(), 12000, 1));
        }
        if (ev.entity instanceof EntityPlayer) {
            ExtendedPlayer ep = ExtendedPlayer.server((EntityPlayer)((EntityPlayer)ev.entity));
            ep.logoutTime = 0L;
            if (ev.ammount >= ep.healDamageCancel) {
                if (ep.healing > 0.0f) {
                    SPacketHandler.sendHint((EntityPlayer)ev.entity, "hi0", "\u00a7c \u041b\u0435\u0447\u0435\u043d\u0438\u0435 \u0441\u0431\u0440\u043e\u0448\u0435\u043d\u043e \u0443\u0440\u043e\u043d\u043e\u043c!", 50);
                }
                ep.healing = 0.0f;
            } else {
                ep.pauseHeal = 2;
            }
        }
    }

    @ForgeSubscribe(priority=EventPriority.LOWEST)
    public void cheaterDamage(LivingHurtEvent ev) {
        if (HcsServer.aprilFool()) {
            ev.entity.worldObj.playSoundAtEntity(ev.entity, "mob.chicken.hurt", 1.0f, 1.1f + rand.nextFloat() * 0.1f);
        }
    }

    @ForgeSubscribe
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entityLiving.isPotionActive((Potion)Effect.bleeding)) {
            if (!(event.entityLiving instanceof EntityPlayer)) {
                event.entityLiving.removePotionEffect(Effect.bleeding.H);
            } else if (event.entityLiving.getActivePotionEffect((Potion)Effect.bleeding).getDuration() == 0) {
                event.entityLiving.removePotionEffect(Effect.bleeding.H);
            } else if (event.entityLiving.q.rand.nextInt(60) == 0) {
                event.entityLiving.attackEntityFrom((DamageSource)DamageType.bleedOut, 0.05f);
            }
        }
        if (event.entityLiving.isPotionActive(Potion.regeneration)) {
            event.entityLiving.removePotionEffect(Potion.regeneration.id);
        }
    }

    @ForgeSubscribe
    public void playerKilledEntity(LivingDeathEvent event) {
        ExtendedPlayer ep;
        if (event.source.getEntity() instanceof EntityPlayer) {
            VSP os2;
            EntityPlayer killer = (EntityPlayer)event.source.getEntity();
            if (event.entityLiving instanceof EntityZombieDayZ) {
                ep = ExtendedPlayer.server((EntityPlayer)killer);
                ++ep.zombieKills;
                try {
                    os2 = new VSP(1, "HCSMOD");
                    os2.writeByte(0);
                    os2.writeInt(ep.zombieKills);
                    os2.send(killer);
                }
                catch (Exception os2) {
                    // empty catch block
                }
            }
            if (event.entityLiving instanceof EntityPlayer) {
                if (HcsServer.playerHasData(((EntityPlayer)event.entityLiving).username, "cheater")) {
                    killer.a(cheater_kill);
                }
                ep = ExtendedPlayer.server((EntityPlayer)killer);
                ++ep.playerKills;
                try {
                    os2 = new VSP(1, "HCSMOD");
                    os2.writeByte(1);
                    os2.writeInt(ep.playerKills);
                    os2.send(killer);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)event.entityLiving;
            ep = ExtendedPlayer.server((EntityPlayer)p);
            ep.healing = 0.0f;
            for (RandomSpawn rs : RandomSpawn.spawns.values()) {
                double z;
                double x;
                if (!(rs.radius > 0.0) || rs.cooldown <= 0L || !((x = p.u - rs.posX) * x + (z = p.w - rs.posZ) * z <= rs.radius * rs.radius)) continue;
                Long time = System.currentTimeMillis() + rs.cooldown * 1000L;
                ep.spawnCooldowns.put(rs.name, time);
            }
        }
    }

    @ForgeSubscribe
    public void onEntitySpawn(LivingSpawnEvent.CheckSpawn event) throws ClassNotFoundException {
        if (event.world.isRemote) {
            return;
        }
        if (event.entity instanceof EntitySnowmanHCS || event.entity instanceof EntitySheep || event.entity instanceof EntityEnderman || event.entity instanceof EntitySkeleton || event.entity instanceof EntityOcelot || event.entity instanceof EntityZombie || event.entity instanceof EntitySpider || event.entity instanceof EntitySlime || event.entity instanceof EntityCreeper) {
            event.setResult(Event.Result.DENY);
            return;
        }
    }

    @ForgeSubscribe
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote) {
            return;
        }
        double SS = 120.0;
        Entity e = event.entity;
        if (e.posX >= -SS && e.posX <= SS && e.posZ >= -SS && e.posZ <= SS) {
            if (event.entity instanceof MCH_EntityThrowable) {
                EntityLivingBase thrower = ((MCH_EntityThrowable)event.entity).h();
                if (thrower instanceof EntityPlayerMP) {
                    ((EntityPlayerMP)thrower).sendChatToPlayer(ChatMessageComponent.createFromText((String)"\u00a7c\u0417\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e \u043a\u0438\u0434\u0430\u0442\u044c \u044d\u0442\u043e \u043d\u0430 \u0441\u0435\u0439\u0444\u0437\u043e\u043d\u0435. \u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u043a\u043e\u043d\u0444\u0438\u0441\u043a\u043e\u0432\u0430\u043d."));
                }
                event.entity.isDead = true;
                event.setCanceled(true);
                return;
            }
            if (event.entity instanceof EntityFishHook) {
                event.entity.isDead = true;
                event.setCanceled(true);
            }
        }
        if (event.entity instanceof EntityItem) {
            EntityItem e2 = (EntityItem)event.entity;
            ItemStack is = e2.getEntityItem();
            if (HcsServer.isStartLoot(is) || HcsServer.isBannedItem(is)) {
                e2.x();
                event.setCanceled(true);
                return;
            }
            SPlayerTracker.turnFlashlightOff(is);
        }
        if (event.entity instanceof EntityZombie) {
            EntityZombieDayZ ezd = new EntityZombieDayZ(event.world);
            ezd.a(event.entity.posX, event.entity.posY, event.entity.posZ, event.entity.rotationYaw, event.entity.rotationPitch);
            ezd.a(null);
            event.world.spawnEntityInWorld((Entity)ezd);
        }
        if (event.entity instanceof EntitySnowmanHCS || event.entity instanceof EntityXPOrb || event.entity instanceof EntitySheep || event.entity instanceof EntityEnderman || event.entity instanceof EntitySkeleton || event.entity instanceof EntityOcelot || event.entity instanceof EntityZombie || event.entity instanceof EntitySpider || event.entity instanceof EntitySlime || event.entity instanceof EntityCreeper) {
            event.setCanceled(true);
            return;
        }
        if (HcsServer.isHarxcoreServer && event.entity instanceof MCH_EntityAircraft && !HcsServer.isAllowedAircraft(((IVehicle)event.entity).vehName())) {
            event.setCanceled(true);
            event.entity.setDead();
            return;
        }
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)event.entity;
            ExtendedPlayer ep = p.q.isRemote ? ExtendedPlayer.client((String)p.username) : ExtendedPlayer.server((EntityPlayer)p);
            p.openContainer = p.inventoryContainer = new ContainerExtended(p, ep.inventory);
        }
    }

    @ForgeSubscribe(priority=EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent ev) {
        if (ev.entity.worldObj.isRemote) {
            return;
        }
        if (ev.action.equals((Object)PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            int b;
            if (!MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(ev.entityPlayer.username)) {
                Vec3 vec = ev.entityPlayer.q.getWorldVec3Pool().getVecFromPool((double)ev.x, (double)ev.y, (double)ev.z);
                ClanPlayer clanPlayer = ClansServer.getClanPlayer(ev.entityPlayer.username);
                for (ServerBase base : ClansServer.bases.values()) {
                    int blockID;
                    if (!base.protectionRegion.isVecInside(vec) || clanPlayer != null && clanPlayer.role.higherOrEquals(ClanPlayer.Role.PRIVATE) && clanPlayer.clan.equals(base.capturedBy)) continue;
                    if (base.state == ClanBase.State.LOCKED) {
                        ev.setCanceled(true);
                        return;
                    }
                    if (base.state != ClanBase.State.DEFENCE || (blockID = ev.entity.worldObj.getBlockId(ev.x, ev.y, ev.z)) != Block.chest.cF) continue;
                    ev.setCanceled(true);
                    return;
                }
            }
            if ((b = ev.entityPlayer.q.getBlockId(ev.x, ev.y, ev.z)) == Block.enchantmentTable.blockID || b == Block.anvil.blockID) {
                ev.entityPlayer.q.setBlockToAir(ev.x, ev.y, ev.z);
                ev.setCanceled(true);
            } else if (ev.entityPlayer.q.getBlockId(ev.x, ev.y, ev.z) == Block.enderChest.blockID) {
                ev.entityPlayer.q.setBlockToAir(ev.x, ev.y, ev.z);
                ev.setCanceled(true);
            } else if (ev.entityPlayer.q.getBlockId(ev.x, ev.y, ev.z) == Block.anvil.blockID) {
                ev.entity.worldObj.setBlockMetadataWithNotify(ev.x, ev.y, ev.z, 0, 2);
            }
        }
    }

    @ForgeSubscribe
    public void death(PlayerDropsEvent ev) {
        ev.entityPlayer.captureDrops = true;
        ExtendedPlayer.server((EntityPlayer)ev.entityPlayer).inventory.dropAllItems(ev.entityPlayer);
        ev.entityPlayer.captureDrops = false;
        Iterator i = ev.drops.iterator();
        while (i.hasNext()) {
            EntityItem ei = (EntityItem)i.next();
            ItemStack is = ei.getEntityItem();
            if (is.stackTagCompound != null && is.stackTagCompound.getBoolean("DAYZSTARTLOOT")) {
                i.remove();
            }
            SPlayerTracker.turnFlashlightOff(is);
        }
    }

    @ForgeSubscribe(priority=EventPriority.LOWEST)
    public void corpse(PlayerDropsEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (!player.q.isRemote) {
            EntityPlayerMP attacker = null;
            if (event.source.getSourceOfDamage() instanceof EntityPlayerMP) {
                attacker = (EntityPlayerMP)event.source.getSourceOfDamage();
            }
            EntityCorpse corpse = new EntityCorpse(player, (List)event.drops, (EntityPlayer)attacker);
            player.q.spawnEntityInWorld((Entity)corpse);
            event.setCanceled(true);
        }
    }

    public static boolean isProtaSet(EntityLivingBase entityLiving) {
        return entityLiving.getCurrentItemOrArmor(1) != null && entityLiving.getCurrentItemOrArmor(2) != null && entityLiving.getCurrentItemOrArmor(3) != null && entityLiving.getCurrentItemOrArmor(4) != null && entityLiving.getCurrentItemOrArmor((int)1).itemID == HCS.prota_boots.itemID && entityLiving.getCurrentItemOrArmor((int)2).itemID == HCS.prota_pants.itemID && entityLiving.getCurrentItemOrArmor((int)3).itemID == HCS.prota_body.itemID && entityLiving.getCurrentItemOrArmor((int)4).itemID == HCS.prota_head.itemID;
    }

    @ForgeSubscribe(priority=EventPriority.HIGHEST)
    public void importatnt_fix(LivingAttackEvent e) {
        if (e.entity.isDead) {
            e.setCanceled(true);
        }
    }
}

