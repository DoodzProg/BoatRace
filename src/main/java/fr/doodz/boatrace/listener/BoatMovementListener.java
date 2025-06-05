package fr.doodz.boatrace.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class BoatMovementListener implements Listener {

    private static final double MAX_VERTICAL_CHECK = 0.7;
    private static final long AIR_PHASE_DURATION_MS = 1000; // Total duration of the hold phase
    private static final long DESCENT_PHASE_DURATION_MS = 500; // Gradual return to normal
    private static final double JUMP_VELOCITY = 0.6; // Higher than before
    private static final double TARGET_BOOST = 1.2; // Peak to reach
    private static final double NORMAL_SPEED = 0.9;

    private final Map<Boat, Long> airStartTime = new HashMap<>();
    private final Set<Boat> recentObstacle = new HashSet<>();
    private final Map<Boat, Double> targetSpeed = new HashMap<>();
    private final Map<Boat, Vector> targetDirection = new HashMap<>();
    private final Map<Boat, Double> integral = new HashMap<>();

    private final double kp = 2.5; // (reactive)
    private final double ki = 0.2; //(reduces cumulative error)

    @EventHandler
    public void onBoatMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat)) return;

        Boat boat = (Boat) event.getVehicle();
        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        Vector velocity = to.clone().subtract(from);
        double horizontalSpeed = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());

        Block blockUnder = boat.getLocation().clone().subtract(0, 0.01, 0).getBlock();
        boolean isInAir = blockUnder.getType() == Material.AIR;
        long now = System.currentTimeMillis();

        if (isInAir && recentObstacle.contains(boat)) {
            airStartTime.putIfAbsent(boat, now);
            long airTime = now - airStartTime.get(boat);

            if (airTime <= AIR_PHASE_DURATION_MS + DESCENT_PHASE_DURATION_MS
                    && targetSpeed.containsKey(boat)
                    && targetDirection.containsKey(boat)) {

                double currentTarget;
                if (airTime <= AIR_PHASE_DURATION_MS) {
                    // Boost phase: maintain the speed peak
                    currentTarget = targetSpeed.get(boat);
                } else {
                    // Smooth descent phase
                    double t = (airTime - AIR_PHASE_DURATION_MS) / (double) DESCENT_PHASE_DURATION_MS;
                    currentTarget = TARGET_BOOST - t * (TARGET_BOOST - NORMAL_SPEED);
                }

                Vector direction = targetDirection.get(boat).clone().normalize();
                Vector currentVel = boat.getVelocity();
                Vector currentHorizontal = new Vector(currentVel.getX(), 0, currentVel.getZ());
                double currentSpeed = currentHorizontal.length();
                double error = currentTarget - currentSpeed;

                double integralValue = integral.getOrDefault(boat, 0.0) + error;
                integral.put(boat, integralValue);

                double adjustment = kp * error + ki * integralValue;

                Vector adjustedVel = direction.multiply(currentSpeed + adjustment)
                        .setY(currentVel.getY());
                boat.setVelocity(adjustedVel);

                if (!boat.getPassengers().isEmpty() && boat.getPassengers().get(0) instanceof Player) {
                    Player player = (Player) boat.getPassengers().get(0);
                    player.sendMessage("§a[PI] speed: " + String.format("%.3f", currentSpeed + adjustment));
                }
            } else {
                // End of jump, reset
                airStartTime.remove(boat);
                recentObstacle.remove(boat);
                targetSpeed.remove(boat);
                targetDirection.remove(boat);
                integral.remove(boat);
            }
        } else {
            // Touch the ground = reset
            airStartTime.remove(boat);
            recentObstacle.remove(boat);
            targetSpeed.remove(boat);
            targetDirection.remove(boat);
            integral.remove(boat);
        }

        if (horizontalSpeed < 0.01) return;

        // Obstacle detection
        double dynamicDistance = horizontalSpeed * 3;
        Vector dir = new Vector(velocity.getX(), 0, velocity.getZ()).normalize();
        Vector boatPos = boat.getLocation().toVector();

        for (double d = 0.5; d <= dynamicDistance; d += 0.5) {
            Vector checkPos = boatPos.clone().add(dir.clone().multiply(d));

            for (double y = 0; y <= MAX_VERTICAL_CHECK; y += 0.3) {
                Vector blockPosVec = new Vector(checkPos.getX(), boatPos.getY() + y, checkPos.getZ());
                Block block = boat.getWorld().getBlockAt(
                        blockPosVec.getBlockX(),
                        blockPosVec.getBlockY(),
                        blockPosVec.getBlockZ()
                );

                if (block.getType() != Material.AIR) {
                    System.out.println("[BoatDebug] Obstacle detected at " + d + " blocks, type: " + block.getType());

                    if (!boat.getPassengers().isEmpty() && boat.getPassengers().get(0) instanceof Player) {
                        boat.getPassengers().get(0).sendMessage(String.format(
                                "§cObstacle at %.2f blocks. Jump with PID hold!", d));
                    }

                    Vector currentVelocity = boat.getVelocity();
                    double newYVel = Math.max(currentVelocity.getY(), JUMP_VELOCITY);
                    boat.setVelocity(new Vector(currentVelocity.getX(), newYVel, currentVelocity.getZ()));

                    // Prepare jump and hold
                    recentObstacle.add(boat);
                    targetSpeed.put(boat, horizontalSpeed + TARGET_BOOST);
                    targetDirection.put(boat, dir);
                    integral.put(boat, 0.0);
                    airStartTime.put(boat, now);
                    return;
                }
            }
        }
    }
}
