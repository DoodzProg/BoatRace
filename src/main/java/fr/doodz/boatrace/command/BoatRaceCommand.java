package fr.doodz.boatrace.command;

import fr.doodz.boatrace.BoatRace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BoatRaceCommand implements CommandExecutor {

    private boolean enabled = true;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            enabled = !enabled;
            sender.sendMessage("§aBoatRace mode toggled to " + (enabled ? "enabled" : "disabled") + "!");
        } else {
            String arg = args[0].toLowerCase();
            if (arg.equals("activate") || arg.equals("true")) {
                enabled = true;
                sender.sendMessage("§aBoatRace mode activated!");
            } else if (arg.equals("deactivate") || arg.equals("false")) {
                enabled = false;
                sender.sendMessage("§cBoatRace mode deactivated!");
            } else {
                sender.sendMessage("§cInvalid argument! Use activate/true, deactivate/false or nothing to toggle.");
                return false;
            }
        }
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
