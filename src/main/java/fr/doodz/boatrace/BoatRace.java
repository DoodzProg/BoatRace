package fr.doodz.boatrace;

import fr.doodz.boatrace.command.BoatRaceCommand;
import fr.doodz.boatrace.listener.BoatMovementListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the BoatRace plugin.
 * Handles plugin lifecycle, configuration loading, command and listener registration.
 */
public class BoatRace extends JavaPlugin {

    private static BoatRace instance;
    private BoatRaceConfig boatRaceConfig;
    private boolean debug = false;

    /**
     * Called when the plugin is enabled.
     * Initializes configuration, registers commands and event listeners.
     */
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        boatRaceConfig = new BoatRaceConfig();
        boatRaceConfig.loadFromConfig(getConfig());

        // Register the /boatrace command and its alias /br
        this.getCommand("boatrace").setExecutor(new BoatRaceCommand(this));

        // Register the listener for boat movement events
        getServer().getPluginManager().registerEvents(new BoatMovementListener(), this);

        getLogger().info("BoatRace plugin enabled!");
    }

    /**
     * Called when the plugin is disabled.
     * Used here to log plugin disable event.
     */
    @Override
    public void onDisable() {
        getLogger().info("BoatRace plugin disabled.");
    }

    /**
     * Static getter for the main plugin instance.
     * @return the singleton plugin instance
     */
    public static BoatRace getInstance() {
        return instance;
    }

    /**
     * Getter for the plugin's configuration wrapper.
     * @return the BoatRaceConfig instance
     */
    public BoatRaceConfig getBoatRaceConfig() {
        return boatRaceConfig;
    }

    /**
     * Checks if debug mode is enabled.
     * @return true if debug mode is active, false otherwise
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets the debug mode.
     * @param debug true to enable debug mode, false to disable
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Reloads the plugin configuration without restarting the server.
     * Useful for development or admin convenience.
     * Re-registers commands and listeners to apply changes immediately.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        boatRaceConfig.loadFromConfig(getConfig());
        getLogger().info("BoatRace configuration reloaded.");

        // Re-register command executor
        this.getCommand("boatrace").setExecutor(new BoatRaceCommand(this));

        // Re-register listeners - consider unregistering old listeners if a clean reload is needed
        getServer().getPluginManager().registerEvents(new BoatMovementListener(), this);

        getLogger().info("Commands and listeners re-registered after reload.");
    }
}
