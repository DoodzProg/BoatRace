package fr.doodz.boatrace;

import fr.doodz.boatrace.command.BoatRaceCommand;
import fr.doodz.boatrace.listener.BoatMovementListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoatRace extends JavaPlugin {

    private static BoatRace instance;
    private BoatRaceCommand boatRaceCommand;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("BoatRace v" + getDescription().getVersion() + " enabled.");

        // Enregistrer listener
        getServer().getPluginManager().registerEvents(new BoatMovementListener(), this);

        // Enregistrer commande
        boatRaceCommand = new BoatRaceCommand();
        this.getCommand("boatrace").setExecutor(boatRaceCommand);

        getLogger().info("BoatRace plugin ready.");
    }

    public static BoatRace getInstance() {
        return instance;
    }

    public boolean isBoatRaceEnabled() {
        return boatRaceCommand != null && boatRaceCommand.isEnabled();
    }
}

