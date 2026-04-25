package me.vesk.trade;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("trade|tr")
@Description("Торговля с игроком")
public class CommandTrade extends BaseCommand {

    private final ManagerTrade tradeManager;

    public CommandTrade(ManagerTrade managerTrade) {
        this.tradeManager = managerTrade;
    }

    @Subcommand("invite")
    @CommandCompletion("@players")
    public void onTrade(CommandSender sender, @Single String TargetPlayerName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return;
        }
        Player InviterPlayer = (Player) sender;
        Player TargetPlayer = Bukkit.getPlayer(TargetPlayerName);
        if (TargetPlayer == null || !TargetPlayer.isOnline()) {
            InviterPlayer.sendMessage("§cИгрок не в сети!");
            return;
        }
        if (InviterPlayer.equals(TargetPlayer)) {
            InviterPlayer.sendMessage("§cНельзя торговать с самим собой!");
            return;
        }
        boolean created = tradeManager.createInvite(InviterPlayer, TargetPlayer);

        if (!created) {
            InviterPlayer.sendMessage("§cВы уже отправили приглашение этому игроку!");
            return;
        }

        InviterPlayer.sendMessage("§aВы пригласили " + TargetPlayer.getName() + " на торговлю!");
        TargetPlayer.sendMessage("§aИспользуйте §6/trade accept " + InviterPlayer.getName() + " §aдля принятия");
    }

    @Subcommand("accept")
    @CommandCompletion("@players")
    public void TradeAccept(CommandSender sender, @Single String InviterPlayerName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return;
        }
        Player TargetPlayer = (Player) sender;
        Player InviterPlayer = Bukkit.getPlayer(InviterPlayerName);
        if (InviterPlayer == null || !InviterPlayer.isOnline()) {
            TargetPlayer.sendMessage("§cИгрок не в сети!");
            return;
        }
        if (TargetPlayer.equals(InviterPlayer)) {
            TargetPlayer.sendMessage("§cНельзя принять самого себя!");
            return;
        }
        if (!(tradeManager.hasActiveInvite(InviterPlayer, TargetPlayer))) {
            TargetPlayer.sendMessage("Никто не приглашал вас к торговле!");
            return;
        }
        tradeManager.startTrade(InviterPlayer, TargetPlayer);
    }
}
