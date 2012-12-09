/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.BotAccessLevel;
import net.boreeas.irc.ChannelAccessLevel;
import net.boreeas.irc.IrcBot;
import net.boreeas.irc.User;
import net.boreeas.irc.Command;

/**
 *
 * @author Boreeas
 */
public class PartCommand extends Command {

    public PartCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "part";
    }

    @Override
    public void execute(User sender, String target, String[] args)
            throws IOException {

        if (args.length == 0) {
            handleNoArgs(sender, target);
        } else {
            handleWithArgs(sender, target, args);
        }
    }

    @Override
    public String help() {
        return "Part the target channel - part [channel] [channel...]";
    }

    private void handleNoArgs(User sender, String target) throws IOException {
        if (!target.startsWith("#")) {
            bot.sendMessage(sender.nick(), "No channel specified");
            return;
        }

        if (require(sender.nick(), target, ChannelAccessLevel.VOICE)
            || require(sender.nick(), BotAccessLevel.MOD)) {

            bot.leaveChannel(target);
        } else {

            bot.sendMessage(target, sender.nick() + ": ChannelAccessLevel VOICE "
                    + "or BotAccessLevel MOD is needed");
        }
    }

    private void handleWithArgs(User sender, String target, String[] args)
            throws IOException {

        BotAccessLevel level = bot.getAccessLevel(sender.nick(), true);

        if (level.compareTo(BotAccessLevel.MOD) >= 0) {
            for (String channel : args) {
                bot.leaveChannel(channel);
            }
        } else {
            nope(sender.nick(), getReplyTarget(sender.nick(), target), level);
        }
    }
}
