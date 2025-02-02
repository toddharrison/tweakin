package com.github.sachin.tweakin.nbtapi.nms;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.github.sachin.tweakin.TweakItem;
import com.github.sachin.tweakin.Tweakin;
import com.github.sachin.tweakin.betterflee.AnimalFleeTweak;
import com.github.sachin.tweakin.mobheads.Head;
import com.github.sachin.tweakin.utils.PaperUtils;
import com.google.common.base.Enums;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import net.minecraft.server.v1_16_R3.*;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;



public class NBTItem_1_16_R3 extends NMSHelper{

    
    private net.minecraft.server.v1_16_R3.ItemStack nmsItem;
    private NBTTagCompound compound;

    public NBTItem_1_16_R3(ItemStack item){
        if(item == null) return;
        ItemStack bukkitItem = item.clone();
        this.nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
        this.compound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
    }

    @Override
    public NMSHelper newItem(ItemStack item) {
        NMSHelper nbti = new NBTItem_1_16_R3(item);
        return nbti;
    }
    
    @Override
    public void setString(String key,String value){
        compound.setString(key, value);
    }

    @Override
    public ItemStack getItem() {
        nmsItem.setTag(compound);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public boolean hasKey(String key) {
        return compound.hasKey(key);
    }

    @Override
    public String getString(String key) {
        return compound.getString(key);
    }

    @Override
    public void removeKey(String key) {
        compound.remove(key);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        compound.setBoolean(key, value);
    }

    @Override
    public boolean getBoolean(String key) {
        return compound.getBoolean(key);
    }

    @Override
    public void setInt(String key, int value) {
        compound.setInt(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        compound.setLong(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        compound.setDouble(key, value);
        
    }

    @Override
    public int getInt(String key) {
        return compound.getInt(key);
    }

    @Override
    public long getLong(String key) {
        return compound.getLong(key);
    }

    @Override
    public double getDouble(String key) {
        return compound.getDouble(key);
    }

    @Override
    public void attack(Player player, Entity target) {
        
        ((CraftPlayer)player).getHandle().attack(((CraftEntity)target).getHandle());
        ((CraftPlayer)player).getHandle().resetAttackCooldown();
        
    }

    public boolean placeItem(Player player, Location location,ItemStack item,BlockFace hitFace,String tweakName){
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        BlockPosition pos = new BlockPosition(location.getX(), location.getY(), location.getZ());
        EntityPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
        MovingObjectPositionBlock mop = new MovingObjectPositionBlock(new Vec3D(location.getX(),location.getY(),location.getZ()),Enums.getIfPresent(EnumDirection.class, hitFace.toString()).or(EnumDirection.DOWN),pos,false);
        
        EnumInteractionResult result = nmsItem.placeItem( new BlockActionContext(nmsPlayer, EnumHand.MAIN_HAND, nmsItem, mop), EnumHand.MAIN_HAND);
        
        if(result == EnumInteractionResult.CONSUME){
            player.swingMainHand();
            player.getWorld().playSound(location, location.getBlock().getBlockData().getSoundGroup().getPlaceSound(), 1F, 1F);
            if(tweakName != null){
                Tweakin plugin = Tweakin.getPlugin();
                if(plugin.placedBlocksMap.containsKey(tweakName)){
                    plugin.placedBlocksMap.put(tweakName, plugin.placedBlocksMap.get(tweakName)+1);
                }
                else{
                    plugin.placedBlocksMap.put(tweakName, 1);
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    public void harvestBlock(Player player,Location location,ItemStack tool){
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(tool);
        EntityPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
        
        BlockPosition pos = new BlockPosition(location.getX(),location.getY(),location.getZ());
        World world = ((CraftWorld)player.getWorld()).getHandle();
        
        IBlockData blockData = world.getType(pos);
        Block nmsBlock = blockData.getBlock();
        
        nmsBlock.a(world, nmsPlayer, pos, blockData, world.getTileEntity(pos), nmsItem);
        world.a(pos,false);
    }

    public int getColor(String str,int transparency){
        String[] array = str.replace(" ", "").split(",");
        if(array == null || array.length == 0){
            return 100;
        }
        if(array.length != 3) return 100;
        int red = Integer.parseInt(array[0]);
        int green = Integer.parseInt(array[1]);
        int blue = Integer.parseInt(array[2]);
        return new Color(red,green,blue,transparency).getRGB();
    }

    public void spawnVillager(Villager villager){
        EntityVillager vil = (EntityVillager) ((CraftEntity)villager).getHandle();
        
        vil.goalSelector.a(2,new FollowPathFinder(vil));
        
    }

    @Override
    public void avoidPlayer(Entity entity,Player player,ConfigurationSection config) {
        
        EntityAnimal animal = (EntityAnimal) ((CraftEntity)entity).getHandle();
        List<EntityAnimal> list = animal.getWorld().a(EntityAnimal.class,animal.getBoundingBox().g(5));
        if(Tweakin.getPlugin().isRunningPaper){
            PaperUtils.removePanicGoal(entity);
        }
        if(!list.isEmpty()){
            for (EntityAnimal en : list) {
                Entity bEn = en.getBukkitEntity();
                if(bEn.getType() == entity.getType()){
                    if(bEn.getPersistentDataContainer().has(AnimalFleeTweak.key, PersistentDataType.INTEGER) && config.getBoolean("ignore-breeded")) continue;
                    en.goalSelector.a(1, new FleePathFinder<EntityPlayer>(en,EntityPlayer.class,config.getInt("max-radius"), config.getDouble("walk-speed"), config.getDouble("sprint-speed"),(pl) -> pl.getUniqueID() == player.getUniqueId(),config.getInt("cooldown")));
                }
            }
        }
    }

    public List<Entity> getEntitiesWithinRadius(int radius,Entity center){
        net.minecraft.server.v1_16_R3.Entity nmsEn = ((CraftEntity)center).getHandle();
        List<Entity> list = nmsEn.getWorld().a(net.minecraft.server.v1_16_R3.Entity.class,nmsEn.getBoundingBox().g(radius)).stream().map(e -> e.getBukkitEntity()).collect(Collectors.toList());
        return list;
        
    }

    @Override
    public boolean matchAxoltlVariant(Entity entity,String color) {
        return false;
    }

    @Override
    public boolean isScreamingGoat(Entity entity) {
        return false;
    }

    private class FleePathFinder<T extends EntityLiving> extends PathfinderGoalAvoidTarget<T>{
        private int tick = 0;
        private int cooldown;

        public FleePathFinder(EntityCreature entity, Class<T> avoider, float maxDis, double walkSpeedModifier, double sprintSpeedModifier,
                Predicate<EntityLiving> condition,int cooldown) {
            super(entity, avoider, maxDis, walkSpeedModifier, sprintSpeedModifier, condition);
            this.cooldown = cooldown*20;
        }

        @Override
        public boolean a() {
            if(tick > cooldown){
                return false;
            }
            else{
                tick++;
                return super.a();
            }
        }
    }


    private class FollowPathFinder extends PathfinderGoal{

        private EntityInsentient a;
        private EntityPlayer player;

        public FollowPathFinder(EntityInsentient var0) {
            this.a = var0;
            a(EnumSet.of(PathfinderGoal.Type.MOVE,PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            List<EntityLiving> list = a.getWorld().a(EntityLiving.class, a.getBoundingBox().g(10));
            if(!list.isEmpty()){
                for(EntityLiving e : list){
                    if(e instanceof EntityPlayer){
                        EntityPlayer target = (EntityPlayer) e;
                        if(CraftItemStack.asBukkitCopy(target.getItemInMainHand()).getType() == Material.EMERALD_BLOCK){
                            this.player = target;
                            return true;
                        }
                        else if(CraftItemStack.asBukkitCopy(target.getItemInOffHand()).getType() == Material.EMERALD_BLOCK){
                            this.player = target;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void c() {
            if(player != null){
                a.getNavigation().a(player.locX(),player.locY(),player.locZ(),0.6);
            }
        }

        @Override
        public boolean b() {
            return this.player.h(this.a) < (double) 10;
        }

        @Override
        public void d() {
            this.player = null;
        }




    }

 
    
}
