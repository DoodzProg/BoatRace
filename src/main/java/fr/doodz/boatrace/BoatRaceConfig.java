package fr.doodz.boatrace;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration wrapper class for the BoatRace plugin.
 * Handles loading and storing config values from the Bukkit config.yml.
 */
public class BoatRaceConfig {

    private boolean onlyForPlayers;
    private boolean boostOnWater;
    private boolean boostUnderwater;
    private boolean boostOnIce;
    private boolean boostOnBlocks;

    private FileConfiguration config;

    // --- Getters and setters ---

    /**
     * Indicates if the plugin should only affect players (ignore boats without players).
     * @return true if only players are affected, false otherwise
     */
    public boolean isOnlyForPlayers() {
        return onlyForPlayers;
    }

    public void setOnlyForPlayers(boolean onlyForPlayers) {
        this.onlyForPlayers = onlyForPlayers;
    }

    /**
     * Whether boosting is enabled when the boat is on water blocks.
     * @return true if boost on water is enabled
     */
    public boolean isBoostOnWater() {
        return boostOnWater;
    }

    public void setBoostOnWater(boolean boostOnWater) {
        this.boostOnWater = boostOnWater;
    }

    /**
     * Whether boosting is enabled when the boat is underwater.
     * @return true if underwater boost is enabled
     */
    public boolean isBoostUnderwater() {
        return boostUnderwater;
    }

    public void setBoostUnderwater(boolean boostUnderwater) {
        this.boostUnderwater = boostUnderwater;
    }

    /**
     * Whether boosting is enabled when the boat is on ice blocks.
     * @return true if boost on ice is enabled
     */
    public boolean isBoostOnIce() {
        return boostOnIce;
    }

    public void setBoostOnIce(boolean boostOnIce) {
        this.boostOnIce = boostOnIce;
    }

    /**
     * Whether boosting is enabled when the boat is on solid blocks.
     * @return true if boost on blocks is enabled
     */
    public boolean isBoostOnBlocks() {
        return boostOnBlocks;
    }

    public void setBoostOnBlocks(boolean boostOnBlocks) {
        this.boostOnBlocks = boostOnBlocks;
    }

    /**
     * Loads configuration values from the provided Bukkit FileConfiguration.
     * This method must be called to refresh the config values when config.yml changes.
     * @param config the Bukkit FileConfiguration to load from
     */
    public void loadFromConfig(FileConfiguration config) {
        this.config = config;  // Store reference to the configuration

        this.onlyForPlayers = config.getBoolean("only-for-players", true);
        this.boostOnWater = config.getBoolean("boost-on-water", true);
        this.boostUnderwater = config.getBoolean("boost-underwater", false);
        this.boostOnIce = config.getBoolean("boost-on-ice", true);
        this.boostOnBlocks = config.getBoolean("boost-on-blocks", true);
    }

    /**
     * Returns the raw Bukkit FileConfiguration object.
     * @return the Bukkit configuration instance
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
