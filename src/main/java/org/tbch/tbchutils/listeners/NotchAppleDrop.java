package org.tbch.tbchutils.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

public class NotchAppleDrop extends EntityListener {
    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!(player.getName().equals("Notch"))) return;

        player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.APPLE, 1));
    }
}
