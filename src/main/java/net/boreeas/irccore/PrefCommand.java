/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.boreeas.irccore;

import java.io.IOException;
import java.util.Arrays;
import net.boreeas.irc.BotAccessLevel;
import net.boreeas.irc.CTCP;
import net.boreeas.irc.ChannelAccessLevel;
import net.boreeas.irc.Command;
import net.boreeas.irc.IrcBot;
import net.boreeas.irc.User;

/**
 *
 * @author Boreeas
 */
public class PrefCommand extends Command {

    public PrefCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "pref";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws IOException {

        if (args.length < 1) {
            bot.sendNotice(bot.getReplyTarget(target, sender.nick()), "Missing arguments. See help for syntax");
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            bot.sendNotice(bot.getReplyTarget(target, sender.nick()), "Registered Preferences: "
                                                                      + Arrays.toString(bot.getPreferences().getRegisteredPrefs()));
        
            return;
        }

        if (args.length < 2) {
            bot.sendNotice(bot.getReplyTarget(target, sender.nick()), "Missing arguments. See help for syntax");
            return;
        }

        if (args[0].equalsIgnoreCase("get")) {
            getPref(bot.getReplyTarget(target, sender.nick()), args[1]);
        } else if (args[0].equalsIgnoreCase("set")) {
            setPref(bot.getReplyTarget(target, sender.nick()), sender, args);
        } else {
            bot.sendNotice(bot.getReplyTarget(target, sender.nick()), "Unknown command 'pref " + args[0] + "'");
        }
    }

    private void getPref(String target, String pref) throws IOException {
        String value = CTCP.bold(bot.getPreferences().getString(target, pref));
        bot.sendNotice(target, "Preference " + CTCP.bold(pref) + " for " + CTCP.bold(target) + " is " + value);
    }

    private void setPref(String target, User user, String[] args) throws IOException {

        if (!bot.isChannel(target)) {
            bot.sendNotice(target, "Channel preferences can only be changed in channel");
            return;
        }

        if (!require(user.nick(), BotAccessLevel.ADMIN) && !require(user.nick(), target, ChannelAccessLevel.OP)) {
            bot.sendNotice("BotAccessLevel ADMIN or ChannelAccessLevel OP is required", target);
            return;
        }

        if (args.length < 3) { // set <pref> <key>
            bot.sendNotice(target, "Not enough arguments: pref set <key> <value>");
            return;
        }

        bot.getPreferences().setString(target, args[1], args[2]);
        bot.sendNotice(target, "Preference " + CTCP.bold(args[1]) + " set to " + CTCP.bold(args[2]));
    }

    @Override
    public String help() {
        return "Gets, lists or changes channel preferences off the bot - 'pref get <key>', 'pref list' or 'pref set <key> <value>'";
    }

}
