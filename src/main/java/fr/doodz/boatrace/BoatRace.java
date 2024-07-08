package fr.doodz.boatrace;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.Speed;

import java.util.HashMap;
import java.util.UUID;

public final class BoatRace extends JavaPlugin implements Listener {
    private final HashMap<UUID, BukkitTask> raceTimers = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("BoatRace has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("BoatRace has been disabled!");
    }

    @EventHandler
    public Vector onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle() && player.getVehicle() instanceof Boat) {
            Boat boat = (Boat) player.getVehicle();
            Location boatLocation = boat.getLocation();

            // Vérifier le bloc 2 blocs en dessous
            double horizontalSpeedFactor = 2;
            Block block2Below = boatLocation.getBlock().getRelative(BlockFace.DOWN, 2);
            Block block3BelowSpeed = boatLocation.getBlock().getRelative(BlockFace.DOWN, 3);
            if (block2Below.getType() == Material.SPONGE) {
                Vector currentVelocity = boat.getVelocity();
                // Appliquer la vélocité modifiée avec une légère augmentation verticale
                boat.setVelocity(currentVelocity.add(new Vector(0, 1.1, 0)));
                player.sendMessage("UP");
                boat.setGravity(false);
            }
            else{
                boat.setGravity(true);
            }
            return null;
        }
        return null;
    }
}


