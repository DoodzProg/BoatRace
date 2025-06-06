package fr.doodz.boatrace.listener;

import fr.doodz.boatrace.BoatRace;
import fr.doodz.boatrace.BoatRaceConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener class to handle boat-related movement and interaction events.
 * Applies custom physics and debug messages according to configuration.
 */
public class BoatMovementListener implements Listener {

    private static final double MIN_HORIZONTAL_SPEED = 0.01;
    private final Map<Boat, Long> lastUpdate = new HashMap<>();
    private final Map<Boat, Long> lastBlockDebugTime = new HashMap<>();
    private static final long BLOCK_DEBUG_COOLDOWN_MS = 500;

    /**
     * Event triggered when a player right-clicks an entity.
     * If the entity is a boat, sends a debug message when debug mode is enabled.
     */
    @EventHandler
    public void onBoatEnter(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Boat) {
            if (BoatRace.getInstance().isDebug()) {
                Player player = event.getPlayer();
                sendDebug(player, "§6" + player.getName() + " boarded a boat.");
            }
        }
    }

    /**
     * Event triggered when a vehicle moves.
     * Handles custom boat physics such as boosting over blocks and jumping.
     */
    @EventHandler
    public void onBoatMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat boat)) return;

        Player player = null;
        if (!boat.getPassengers().isEmpty() && boat.getPassengers().get(0) instanceof Player p) {
            player = p;
        }

        // If configured to only affect boats ridden by players, skip otherwise
        if (BoatRace.getInstance().getBoatRaceConfig().isOnlyForPlayers() && player == null) return;

        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        Vector movement = to.clone().subtract(from);

        // Calculate horizontal speed (XZ plane)
        double horizontalSpeed = Math.sqrt(movement.getX() * movement.getX() + movement.getZ() * movement.getZ());

        long now = System.currentTimeMillis();

        // Log horizontal speed debug message every 500ms
        if (!lastUpdate.containsKey(boat) || now - lastUpdate.get(boat) > 500) {
            if (BoatRace.getInstance().isDebug()) {
                sendDebug(player, "§eHorizontal speed: §f" + String.format("%.3f", horizontalSpeed));
            }
            lastUpdate.put(boat, now);
        }

        // Skip processing if speed is below threshold
        if (horizontalSpeed < MIN_HORIZONTAL_SPEED) return;

        // Normalize horizontal movement direction
        Vector direction = movement.clone().setY(0).normalize();

        // Calculate anticipation distance (1 to 2.5 blocks) based on speed
        double anticipationDistance = 1.0 + horizontalSpeed * 1.5;
        Vector frontPos = boat.getLocation().toVector().add(direction.multiply(anticipationDistance));

        // Get blocks ahead at foot level, +1, and +2 blocks up
        Block blockAtFoot = boat.getWorld().getBlockAt(frontPos.getBlockX(), frontPos.getBlockY(), frontPos.getBlockZ());
        Block blockOneUp = boat.getWorld().getBlockAt(frontPos.getBlockX(), frontPos.getBlockY() + 1, frontPos.getBlockZ());
        Block blockTwoUp = boat.getWorld().getBlockAt(frontPos.getBlockX(), frontPos.getBlockY() + 2, frontPos.getBlockZ());

        // Debug block types in front every 500ms
        if (BoatRace.getInstance().isDebug()) {
            long lastDebug = lastBlockDebugTime.getOrDefault(boat, 0L);
            if (now - lastDebug >= BLOCK_DEBUG_COOLDOWN_MS) {
                sendDebug(player, "§7Block ahead at foot: §f" + blockAtFoot.getType());
                sendDebug(player, "§7Block ahead +1: §f" + blockOneUp.getType());
                sendDebug(player, "§7Block ahead +2: §f" + blockTwoUp.getType());
                lastBlockDebugTime.put(boat, now);
            }
        }

        // Determine jump height needed to clear obstacles (0 = no jump)
        int jumpHeight = 0;
        if (!isPassable(blockAtFoot)) jumpHeight = 1;
        if (jumpHeight == 1 && !isPassable(blockOneUp)) jumpHeight = 2;
        if (jumpHeight > 2) jumpHeight = 2;

        // If jump is needed and allowed, apply vertical boost to boat velocity
        if (jumpHeight > 0 && shouldJump(blockAtFoot)) {
            double baseBoost = BoatRace.getInstance().getBoatRaceConfig().getConfig().getDouble("physics.boost_vertical_force", 0.6);
            double speedFactor = Math.min(1.0 + horizontalSpeed * 0.7, 1.8);
            double boostVertical = baseBoost * speedFactor * (0.7 + 0.4 * jumpHeight);

            Vector horizontalMovement = to.clone().subtract(from).setY(0);
            double currentHorizontalSpeed = horizontalMovement.length();

            if (currentHorizontalSpeed > 0) {
                Vector horizontalDirection = horizontalMovement.normalize();
                Vector newVelocity = horizontalDirection.multiply(currentHorizontalSpeed);
                newVelocity.setY(boostVertical);
                boat.setVelocity(newVelocity);

                if (BoatRace.getInstance().isDebug()) {
                    sendDebug(player, "§aJUMP triggered! Height: " + jumpHeight + ", vertical boost: " + String.format("%.3f", boostVertical));
                }
            }
        }

        // Special debug message when boat slides on ice blocks
        if (BoatRace.getInstance().isDebug() && blockAtFoot.getType().toString().contains("ICE")) {
            sendDebug(player, "§bBoat is sliding on ice!");
        }
    }

    /**
     * Checks if the block is passable for the boat (air, water, or bubble column).
     * @param block block to check
     * @return true if passable, false otherwise
     */
    private boolean isPassable(Block block) {
        Material type = block.getType();
        return type == Material.AIR || type == Material.WATER || type == Material.BUBBLE_COLUMN;
    }

    /**
     * Determines if the boat should perform a jump boost over the given block,
     * based on configured boost settings.
     * @param block block ahead of the boat
     * @return true if jump boost should be applied, false otherwise
     */
    private boolean shouldJump(Block block) {
        BoatRaceConfig config = BoatRace.getInstance().getBoatRaceConfig();
        Material type = block.getType();

        if (type == Material.AIR) return false;
        if (type == Material.WATER || type == Material.BUBBLE_COLUMN)
            return config.isBoostOnWater() || config.isBoostUnderwater();
        if (type.name().contains("ICE"))
            return config.isBoostOnIce();
        return config.isBoostOnBlocks();
    }

    /**
     * Sends a debug message to both the console and optionally the player.
     * Strips color codes for the console output.
     * @param player player to send the message to (can be null)
     * @param msg message to send
     */
    private void sendDebug(Player player, String msg) {
        BoatRace.getInstance().getLogger().info("[BoatDebug] " + msg.replaceAll("§[0-9a-fk-or]", ""));
        if (player != null) player.sendMessage(msg);
    }
}
