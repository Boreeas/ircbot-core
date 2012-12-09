/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.Command;
import net.boreeas.irc.IrcBot;
import net.boreeas.irc.NoSuchCommandException;
import net.boreeas.irc.User;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Boreeas
 */
public class HelpCommand extends Command {

    public HelpCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "help";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws IOException {

        if (args.length == 0) {

            String[] prefixes = bot.getCommandHandler().getRegisteredPrefixes();
            String prefixString = StringUtils.join(prefixes, ", ");
            String reply = "Available prefix: %s. Use 'help <prefix>' to get a detailed list of commands";

            bot.sendNotice(sender.nick(), String.format(reply, prefixString));

        } else if (args.length == 1) {

            String[] commands = bot.getCommandHandler().getRegisteredCommands(args[0]);
            String commandString = StringUtils.join(commands, ", ");
            String reply = "Commands registered under %s: %s. Use 'help %s <command>' for more detailed help";

            bot.sendNotice(sender.nick(), String.format(reply, args[0], commandString, args[0]  ));
        } else {

            try {
                String help = bot.getCommandHandler().getHelp(args[0], args[1]);
                bot.sendNotice(sender.nick(), args[0] + " " + args[1] + ": " + help);
            } catch (NoSuchCommandException ex) {
                bot.sendNotice(sender.nick(), ex.getMessage());
            }
        }
    }

    @Override
    public String help() {
        return "Lists prefixes, commands, or command help - help [prefix [command]]";
    }

}
