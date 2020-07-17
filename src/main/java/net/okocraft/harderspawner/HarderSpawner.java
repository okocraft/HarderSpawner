package net.okocraft.harderspawner;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class HarderSpawner extends JavaPlugin implements Listener {

    private static final Random random = new Random();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawn(SpawnerSpawnEvent event) {
        Location spawnerLocation = event.getSpawner().getBlock().getLocation();
        Collection<Entity> players = spawnerLocation.getWorld().getNearbyEntities(spawnerLocation, 17, 17, 17, entity -> entity instanceof Player);
        if (players.isEmpty()) {
            return;
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if (!event.isCancelled()) {
                    event.getEntity().teleport(getNearRandomLocation((Player) players.iterator().next()));
                }
            }
        }.runTask(this);
    }

    private static Location getNearRandomLocation(Player player) {
        Location playerLoc = player.getLocation().clone();
        playerLoc.add(random.nextInt(8) - 4, 0, random.nextInt(8) - 4);
        if (!playerLoc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
            return player.getLocation().clone();
        }

        BoundingBox playerBody = player.getBoundingBox();
        playerBody.shift(playerLoc.toVector().subtract(playerBody.getCenter()));
        Location checking = new Location(playerLoc.getWorld(), 0, 0, 0);
        for (int i = (int) playerBody.getMinX(); i <= (int) playerBody.getMaxX(); i++) {
            for (int j = (int) playerBody.getMinY(); j <= (int) playerBody.getMaxY(); j++) {
                for (int k = (int) playerBody.getMinZ(); k <= (int) playerBody.getMaxZ(); k++) {
                    checking.setX(i);
                    checking.setY(j);
                    checking.setZ(k);
                    if (checking.getBlock().getType() != Material.AIR) {
                        return player.getLocation().clone();
                    }
                }
            }
        }

        return playerLoc;
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) {
            return;
        }
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
}
