package com.darkender.plugins.accuracy;

import org.bukkit.Material;
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
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
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
                right.rotateAroundNonUnitAxis(head, -10 * (Math.PI / 180.0));
                
                double current = event.getEntity().getVelocity().length();
                if(right.distanceSquared(velocityDirection) < 0.001)
                {
                    event.getEntity().setVelocity(right.multiply(current));
                }
                else
                {
                    Vector left = p.getEyeLocation().getDirection().clone();
                    left.rotateAroundNonUnitAxis(head, 10 * (Math.PI / 180.0));
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
            // There's a better way to do this for sure
            // Right now it's comparing the entity's block with the source block to form a normal vector
            fixVelocity(event.getEntity(),
                    ((BlockProjectileSource) source).getBlock().getFace(event.getEntity().getLocation().getBlock()).getDirection());
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
        if(item == null)
        {
            return false;
        }
    
        return item.getType() == Material.CROSSBOW && item.getEnchantmentLevel(Enchantment.MULTISHOT) != 0;
    }
    
    private void fixVelocity(Entity entity, Vector direction)
    {
        double current = entity.getVelocity().length();
        entity.setVelocity(direction.clone().normalize().multiply(current));
    }
}
