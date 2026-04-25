package me.vesk.trade;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Trade extends JavaPlugin {

    private PaperCommandManager commandManager;
    private ManagerTrade managerTrade;

    @Override
    public void onEnable() {
        // Инициализируем менеджер
        managerTrade = new ManagerTrade(this);

        // Инициализируем ACF
        commandManager = new PaperCommandManager(this);

        // Регистрируем команды
        commandManager.registerCommand(new CommandTrade(managerTrade));
        getLogger().info("TradePlugin успешно включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TradePlugin выключен!");
    }
}
