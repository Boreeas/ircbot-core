package net.boreeas.irccore;

import net.boreeas.irc.IrcBot;
import net.boreeas.irc.plugins.Plugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hello world!
 * <p/>
 */
public class Core implements Plugin {

    public static final String CMD_LISTEN_TO_NAME = "cmd-listen-to-name";

    private static final Log logger = LogFactory.getLog("Core");

    @Override
    public void onEnable(IrcBot bot) {

        logger.info("Loading core module");
        bot.getCommandHandler().registerCommand(this, new JoinCommand(bot));
        bot.getCommandHandler().registerCommand(this, new PartCommand(bot));
        bot.getCommandHandler().registerCommand(this, new RawCommand(bot));
        bot.getCommandHandler().registerCommand(this, new AccessCommand(bot));
        bot.getCommandHandler().registerCommand(this, new QuitCommand(bot));
        bot.getCommandHandler().registerCommand(this, new JumpCommand(bot));
        bot.getCommandHandler().registerCommand(this, new NickCommand(bot));
        bot.getCommandHandler().registerCommand(this, new PluginCommand(bot));
        bot.getCommandHandler().registerCommand(this, new MuteCommand(bot));
        bot.getCommandHandler().registerCommand(this, new HelpCommand(bot));
        bot.getCommandHandler().registerCommand(this, new PrefCommand(bot));
        bot.registerEventListener(this, new CoreEventListener(bot));
    }

    @Override
    public void onDisable() {
        logger.info("Unloading core module");
    }

    @Override
    public String getDescription() {
        return "Module providing core functionality for the bot";
    }

    @Override
    public String getPluginName() {
        return "Core";
    }

    @Override
    public String getCommandPrefix() {
        return "!";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String reloadTarget() {
        return "Core";
    }

    @Override
    public void save() {
        // Nothing needed to be done
    }
}
