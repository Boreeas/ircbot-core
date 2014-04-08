/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import net.boreeas.irc.*;
import net.boreeas.irc.plugins.Plugin;
import net.boreeas.irc.plugins.PluginLoadException;
import org.apache.commons.lang.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Boreeas
 */
public class PluginCommand extends Command {

    private static final String help =
                                CTCP.bold("[\"help\"]") + " - Displays help "
                                + "| " + CTCP.bold(
            "[\"list\" | \"show\"] "
            + "[\"loaded\"|\"available\"]")
                                + " - Lists loaded or available plugins "
                                + "| " + CTCP.bold("[\"details\"] <plugin>")
                                + " - List details about <plugin> "
                                + "| " + CTCP.bold("[\"load\" | \"unload\" | "
                                                   + "\"reload\"] <plugin>")
                                + " - Loades, unloades or reloades <plugin>";

    public PluginCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "plugin";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws
            IOException {

        String sendTo = bot.getReplyTarget(sender.nick(), target);

        if (args.length == 0 || args[0].equals("help")) {

            sendHelp(sendTo);
        } else if (args[0].equalsIgnoreCase("show")
                   || args[0].equalsIgnoreCase("list")) {

            listPlugins(sendTo, args);
        } else if (args[0].equalsIgnoreCase("details")) {

            listDetails(sendTo, args);
        } else {

            if (!require(sender.nick(), BotAccessLevel.ADMIN)) {

                nope(sender.nick(), sendTo, BotAccessLevel.ADMIN);
            } else if (args[0].equalsIgnoreCase("load")) {

                loadPlugin(sendTo, args);
            } else if (args[0].equalsIgnoreCase("unload")) {

                unloadPlugin(sendTo, args);
            } else if (args[0].equalsIgnoreCase("reload")) {

                reloadPlugin(sendTo, args);
            }
        }
    }

    @Override
    public String help() {
        return "Working with plugins. '" + getTrigger() + " help' for more detailed information";
    }

    private void sendHelp(String target) throws IOException {

        bot.sendNotice(target, help);
    }

    private void listPlugins(String target, String[] args) throws IOException {

        if (args.length == 1 || args[1].equalsIgnoreCase("loaded")) {

            bot.sendNotice(target, "Loaded plugins: " + StringUtils.join(bot.getPluginManager().loadedPlugins(), " "));
        } else if (args[1].equalsIgnoreCase("available")) {

            bot.sendNotice(target, "Available plugins: " + StringUtils.join(bot.getPluginManager().availablePlugins(), " "));
        } else {

            bot.sendNotice(target, "Invalid option. Format: show [\"loaded\" "
                                   + "| \"available\"]");
        }
    }

    private void listDetails(String target, String[] args) throws IOException {

        if (args.length == 1) {
            bot.sendNotice(target, "Missing arguments. Format: details <plugin>");
        } else {

            Plugin plugin = bot.getPluginManager().getPlugin(args[1]);

            if (plugin == null) {

                bot.sendNotice(target, "No plugin with that name");
            } else {

                bot.sendNotice(target, CTCP.bold(args[1])
                                       + ": Name :: " + plugin.getPluginName()
                                       + " · Version :: " + plugin.getVersion()
                                       + " · Description :: "
                                       + plugin.getDescription()
                                       + " · Command Prefix :: "
                                       + plugin.getCommandPrefix());
            }
        }
    }

    private void loadPlugin(String target, String[] args)
            throws IOException {

        if (args.length == 1) {
            bot.sendNotice(target,
                           "Missing arguments. Format: load <filename> [filename...]");
        } else {

            for (int i = 1; i < args.length; i++) {
                String pluginName = args[i];

                try {

                    Plugin plugin =
                           bot.getPluginManager().loadPlugin(pluginName);
                    bot.sendNotice(target, "Loaded plugin "
                                           + CTCP.bold(plugin.getPluginName())
                                           + " (Version "
                                           + CTCP.bold(plugin.getVersion())
                                           + ") from file " + pluginName);
                } catch (PluginLoadException ex) {

                    bot.sendNotice(target, "Error while enabling plugin: "
                                           + ex.getCause().getMessage());
                    log.error("Error while enabling plugin " + pluginName, ex);
                } catch (FileNotFoundException ex) {

                    bot.sendNotice(target, "No such file: " + pluginName);
                } catch (IOException ex) {

                    bot.sendNotice(target, "Error while loading plugin: " + ex.
                            getCause());
                    log.error("Error while loading plugin " + pluginName, ex);
                }
            }
        }
    }

    private void unloadPlugin(String target, String[] args) throws IOException {

        if (args.length == 1) {
            bot.sendNotice(target, "Missing arguments. Format: unload "
                                   + "<plugin> [plugin...]");
        } else {

            for (int i = 1; i < args.length; i++) {
                String plugin = args[i];

                boolean result = bot.getPluginManager().disablePlugin(plugin);
                bot.sendNotice(target, result
                                       ? plugin + " unloaded"
                                       : "No active plugin: " + plugin);
            }
        }
    }

    private void reloadPlugin(String target, String[] args) throws IOException {

        if (args.length == 1) {
            bot.sendNotice(target, "Missing arguments. Format: reload <plugin> "
                                   + "[plugin...]");
        } else {

            for (int i = 1; i < args.length; i++) {
                String pluginName = args[i];

                Plugin plugin = bot.getPluginManager().getPlugin(pluginName);
                if (plugin == null) {

                    bot.sendNotice(target, "No active plugin with name "
                                           + pluginName);
                } else {

                    bot.getPluginManager().disablePlugin(plugin);

                    try {

                        bot.getPluginManager().loadPlugin(plugin.reloadTarget());
                        bot.sendNotice(target, CTCP.bold(plugin.getPluginName())
                                               + " successfully reloaded (Reload "
                                               + "target was "
                                               + CTCP.bold(plugin.reloadTarget())
                                               + ")");
                    } catch (FileNotFoundException ex) {

                        bot.sendNotice(target, CTCP.bold(plugin.getPluginName())
                                               + " unloaded. Reload target "
                                               + CTCP.bold(plugin.reloadTarget())
                                               + " could not be found.");
                    } catch (PluginLoadException ex) {

                        bot.sendNotice(target, CTCP.bold(plugin.getPluginName())
                                               + " unloaded. Error while reloading "
                                               + CTCP.bold(plugin.reloadTarget()));
                    }
                }
            }
        }
    }
}
