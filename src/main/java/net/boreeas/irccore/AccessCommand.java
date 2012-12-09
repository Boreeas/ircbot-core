/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.BotAccessLevel;
import net.boreeas.irc.IrcBot;
import net.boreeas.irc.User;
import net.boreeas.irc.Command;

/**
 *
 * @author Boreeas
 */
public class AccessCommand extends Command {

    public AccessCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "access";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws
            IOException {

        if (args.length == 0) {

            executeAccessShowHelp(sender, target);
        } else {

            String subCmd = args[0].toLowerCase();

            if (subCmd.equals("show") || subCmd.equals("list")) {

                if (args.length < 2) {
                    executeAccessShow(sender, target, sender.nick());
                } else {
                    executeAccessShow(sender, target, args[1]);
                }
            } else if (subCmd.equals("help")) {

                executeAccessShowHelp(sender, target);
            } else if (subCmd.equals("set")) {

                if (args.length < 3) {
                    bot.sendNotice(sender.nick(),
                                   "Not enough args: set <nick> <level>");
                } else {
                    executeAccessSet(sender, target, args[1], args[2]);
                }
            }
        }
    }

    @Override
    public String help() {
        return "Changes access levels of users - 'access help' for more detailed help";
    }

    private void executeAccessShowHelp(User sender, String target) throws
            IOException {

        String sendTo = getReplyTarget(sender.nick(), target);

        bot.sendNotice(sendTo, "access [help] -- Display help "
                               + "| access <show|list> [nick] -- Display access level for self"
                               + " or [nick] "
                               + "| access <set> <nick> <level> -- Sets nick's access level to"
                               + " <level>");
    }

    private void executeAccessShow(User sender, String target, String toCheck)
            throws IOException {

        String sendTo = getReplyTarget(sender.nick(), target);

        String accName = bot.getAccountName(toCheck);
        BotAccessLevel level = bot.getAccessLevel(accName, false);

        bot.sendNotice(sendTo, toCheck + " (Accountname '" + accName
                               + "') has access level " + level);
    }

    private void executeAccessSet(User sender, String msgTarget, String target,
                                  String level) throws IOException {

        String sendTo = getReplyTarget(sender.nick(), msgTarget);

        try {

            String accName = bot.getAccountName(target);
            BotAccessLevel oldAccessLevel = bot.getAccessLevel(accName, false);

            if (oldAccessLevel == BotAccessLevel.NOT_REGISTERED) {
                bot.sendNotice(sendTo, target + " has not registered or logged "
                                       + "in");
                return;
            }


            BotAccessLevel newAccessLevel = BotAccessLevel.valueOf(level.
                    toUpperCase());

            BotAccessLevel senderLevel = bot.getAccessLevel(sender.nick(), true);

            if (senderLevel.compareTo(newAccessLevel) > 0) {
                if (senderLevel.compareTo(oldAccessLevel) > 0) {

                    // Successful update
                    bot.updateAccessLevel(accName, newAccessLevel);

                    bot.sendNotice(sendTo, target + " (Accountname '" + accName
                                           + "') has access level "
                                           + newAccessLevel);
                } else {

                    // Prevent bot takeovers
                    bot.sendNotice(sendTo, "Can not demote people of higher "
                                           + "rank");
                }
            } else {

                // Prevent access escalation
                bot.sendNotice(sendTo, senderLevel + " can't promote to "
                                       + newAccessLevel);
            }
        } catch (IllegalArgumentException ex) {
            bot.sendNotice(sendTo, "No access level '" + level + "'");
        }
    }
}
