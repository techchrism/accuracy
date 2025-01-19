package com.darkender.plugins.accuracy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class Accuracy extends JavaPlugin implements Listener
{
    private Material crossbow = null;
    private Enchantment multishot = null;
    
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        try
        {
            crossbow = Material.valueOf("CROSSBOW");
            for(Enchantment enchantment : Enchantment.values())
            {
                if(enchantment.getKey().getNamespace().equals("minecraft") && enchantment.getKey().getKey().equals("multishot"))
                {
                    multishot = enchantment;
                }
            }
        }
        catch(Exception e)
        {
            crossbow = null;
            multishot = null;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileFire(ProjectileLaunchEvent event)
    {
        ProjectileSource source = event.getEntity().getShooter();
        if(source instanceof Player)
        {
            Player p = (Player) source;
            
            // Treat multishot crossbows specially
            // This event gets called once for each arrow
            if(isMultishotCrossbow(p.getInventory().getItemInMainHand()) || isMultishotCrossbow(p.getInventory().getItemInOffHand()))
            {
                Vector velocityDirection = event.getEntity().getVelocity().clone().normalize();
                
                // Get the unit vector coming out the top of the player's head
                Vector head = getVectorFromPitch(p.getEyeLocation().getPitch() - 90.0F, p.getEyeLocation().getYaw()).normalize();
                
                // Rotate around the head vector to get the left and right positions
                Vector right = p.getEyeLocation().getDirection().clone();
                OldVersionCompatibility.rotateAroundNonUnitAxis(right, head, -10 * (Math.PI / 180.0));
                
                double current = event.getEntity().getVelocity().length();
                if(right.distanceSquared(velocityDirection) < 0.001)
                {
                    event.getEntity().setVelocity(right.multiply(current));
                }
                else
                {
                    Vector left = p.getEyeLocation().getDirection().clone();
                    OldVersionCompatibility.rotateAroundNonUnitAxis(left, head, 10 * (Math.PI / 180.0));
                    if(left.distanceSquared(velocityDirection) < 0.001)
                    {
                        event.getEntity().setVelocity(left.multiply(current));
                    }
                    else
                    {
                        event.getEntity().setVelocity(p.getEyeLocation().getDirection().clone().multiply(current));
                    }
                }
            }
            else
            {
                fixVelocity(event.getEntity(), p.getEyeLocation().getDirection());
            }
        }
        else if(source instanceof BlockProjectileSource)
        {
            Block sourceBlock = ((BlockProjectileSource) source).getBlock();
            BlockData sourceBlockData = sourceBlock.getBlockData();
            if(sourceBlockData instanceof Directional)
            {
                Directional d = (Directional)sourceBlockData;
                fixVelocity(event.getEntity(), OldVersionCompatibility.getBlockFaceDirection(d.getFacing()));
            }
            else
            {
                Block eventBlock = event.getEntity().getLocation().getBlock();
                BlockFace face = sourceBlock.getFace(eventBlock);
                Vector direction =  OldVersionCompatibility.getBlockFaceDirection(face);
                if(direction != null)
                    fixVelocity(event.getEntity(), direction);
            }
        }
    }
    
    private Vector getVectorFromPitch(float pitch, float yaw)
    {
        double pitchRad = pitch * (Math.PI / 180);
        double yawRad = -yaw * (Math.PI / 180);
    
        double cosYawRad = Math.cos(yawRad);
        double sinYawRad = Math.sin(yawRad);
        double cosPitchRad = Math.cos(pitchRad);
        double sinPitchRad = Math.sin(pitchRad);
    
        return new Vector((sinYawRad * cosPitchRad), -sinPitchRad, (cosYawRad * cosPitchRad));
    }
    
    private boolean isMultishotCrossbow(ItemStack item)
    {
        if(item == null || crossbow == null || multishot == null)
        {
            return false;
        }
    
        return item.getType() == crossbow && item.getEnchantmentLevel(multishot) != 0;
    }
    
    private void fixVelocity(Entity entity, Vector direction)
    {
        double current = entity.getVelocity().length();
        entity.setVelocity(direction.clone().normalize().multiply(current));
    }
}
