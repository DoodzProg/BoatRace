package fr.doodz.boatrace.command;

import fr.doodz.boatrace.BoatRace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoatRaceCommand implements CommandExecutor, TabCompleter {

    private final BoatRace plugin;

    public BoatRaceCommand(BoatRace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "debug" -> {
                if (args.length == 1) {
                    // Toggle debug mode
                    boolean newState = !plugin.isDebug();
                    plugin.setDebug(newState);
                    plugin.getConfig().set("debug", newState);
                    plugin.saveConfig();
                    sender.sendMessage("§6[BoatRace] Debug mode is now §e" + (newState ? "enabled" : "disabled") + "§6.");
                } else if (args.length == 2) {
                    String value = args[1].toLowerCase();
                    if (value.equals("on")) {
                        plugin.setDebug(true);
                        plugin.getConfig().set("debug", true);
                        plugin.saveConfig();
                        sender.sendMessage("§6[BoatRace] Debug mode enabled.");
                    } else if (value.equals("off")) {
                        plugin.setDebug(false);
                        plugin.getConfig().set("debug", false);
                        plugin.saveConfig();
                        sender.sendMessage("§6[BoatRace] Debug mode disabled.");
                    } else {
                        sender.sendMessage("§c[BoatRace] Invalid argument. Use /br debug [on|off]");
                    }
                } else {
                    sender.sendMessage("§c[BoatRace] Usage: /br debug [on|off]");
                }
                return true;
            }

            case "reload" -> {
                plugin.reloadPluginConfig();
                sender.sendMessage("§6[BoatRace] Configuration reloaded.");
                return true;
            }

            case "status" -> {
                sender.sendMessage("§6[BoatRace] Plugin status:");
                sender.sendMessage(" - Debug mode: §e" + (plugin.isDebug() ? "enabled" : "disabled"));
                sender.sendMessage(" - Version: §e" + plugin.getDescription().getVersion());
                return true;
            }

            case "version" -> {
                sender.sendMessage("§6[BoatRace] Version: §e" + plugin.getDescription().getVersion());
                return true;
            }

            case "help" -> {
                sendHelp(sender);
                return true;
            }

            default -> {
                sender.sendMessage("§c[BoatRace] Unknown command. Type §e/br help§c for help.");
                return true;
            }
        }
    }

    /**
     * Sends the help message listing available commands.
     * @param sender the command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6--- §eBoatRace Help §6---");
        sender.sendMessage("§e/br debug [on|off]§7 - Toggle or explicitly enable/disable debug mode");
        sender.sendMessage("§e/br reload§7 - Reload plugin configuration without restarting the server");
        sender.sendMessage("§e/br status§7 - Display current plugin status");
        sender.sendMessage("§e/br version§7 - Display plugin version");
        sender.sendMessage("§e/br help§7 - Show this help message");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("debug", "reload", "status", "version", "help");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return Arrays.asList("on", "off");
        }

        return Collections.emptyList();
    }
}
