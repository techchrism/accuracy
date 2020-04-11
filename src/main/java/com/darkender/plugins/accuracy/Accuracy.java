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
            
            // Ignore multishot crossbows
            if(!isAllowedSource(p.getInventory().getItemInMainHand()) || !isAllowedSource(p.getInventory().getItemInOffHand()))
            {
                return;
            }
            
            fixVelocity(event.getEntity(), p.getEyeLocation().getDirection());
        }
        else if(source instanceof BlockProjectileSource)
        {
            // There's a better way to do this for sure
            // Right now it's comparing the entity's block with the source block to form a normal vector
            fixVelocity(event.getEntity(),
                    ((BlockProjectileSource) source).getBlock().getFace(event.getEntity().getLocation().getBlock()).getDirection());
        }
    }
    
    private boolean isAllowedSource(ItemStack item)
    {
        if(item == null)
        {
            return true;
        }
        
        if(item.getType() == Material.CROSSBOW && item.getEnchantmentLevel(Enchantment.MULTISHOT) != 0)
        {
            return false;
        }
        
        return true;
    }
    
    private void fixVelocity(Entity entity, Vector direction)
    {
        double current = entity.getVelocity().length();
        entity.setVelocity(direction.clone().normalize().multiply(current));
    }
}
