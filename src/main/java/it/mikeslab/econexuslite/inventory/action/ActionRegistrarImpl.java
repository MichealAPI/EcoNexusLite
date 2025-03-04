package it.mikeslab.econexuslite.inventory.action;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.mikeslab.commons.api.component.ComponentsUtil;
import it.mikeslab.commons.api.inventory.pojo.action.GuiAction;
import it.mikeslab.commons.api.inventory.util.action.ActionRegistrar;
import it.mikeslab.econexuslite.EcoNexusLite;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Code extracted from
 * <a href="https://github.com/MichealAPI/Identity/blob/master/src/main/java/it/mikeslab/identity/inventory/action/ActionRegistrarImpl.java">Identity</a>
 */
@RequiredArgsConstructor
public class ActionRegistrarImpl implements ActionRegistrar {

    private final EcoNexusLite instance;

    @Override
    public Multimap<String, GuiAction> loadActions() {

        Multimap<String, GuiAction> actionsMap = ArrayListMultimap.create();

        // Register the open gui action
        actionsMap.put("message", sendMessageToPlayer());
        actionsMap.put("player", executeCommandForPlayer());
        actionsMap.put("console", executeCommandForConsole());
        actionsMap.put("title", sendTitleToPlayer());
        actionsMap.put("sound", playSoundForPlayer());
        actionsMap.put("close", closeInventory());
        actionsMap.put("potion", potionEffect());

        // todo how to open the inventory
        // todo withdraw action? or maybe relate it to player command and update the inventory.

        return actionsMap;

    }


    private GuiAction sendMessageToPlayer() {
        return new GuiAction((event, args) -> {
            Player player = event.getWhoClicked();
            Component message = ComponentsUtil.getComponent(args);

            if(message == null) return;

            instance
                    .getLabCommons()
                    .getAudiences()
                    .player(player)
                    .sendMessage(message);
        });
    }

    private GuiAction executeCommandForPlayer() {
        return new GuiAction((event, args) -> {

            Player player = event.getWhoClicked();
            player.performCommand(args);

        });
    }


    private GuiAction executeCommandForConsole() {
        return new GuiAction((event, args) -> {

            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            console.sendMessage(args);

        });
    }

    private GuiAction sendTitleToPlayer() {
        return new GuiAction((event, args) -> {

            Player player = event.getWhoClicked();
            String[] split = args.split(";");
            player.sendTitle(
                    ComponentsUtil.getSerializedComponent(split[0]),
                    ComponentsUtil.getSerializedComponent(split[1]),
                    10,
                    70,
                    20
            );

        });
    }

    private GuiAction playSoundForPlayer() {
        return new GuiAction((ev, args) -> {

            Player target = ev.getWhoClicked();

            XSound.Record xSound = XSound.parse(args);

            if(xSound == null) {
                return;
            }

            xSound.soundPlayer()
                    .forPlayers(target)
                    .play();
        });
    }

    private GuiAction potionEffect() {
        return new GuiAction((ev, args) -> {

            // Format: Potion, Duration (in seconds), Amplifier (level) [%chance]
            // WEAKNESS, 30, 1
            // SLOWNESS 200 10
            // 1, 10000, 100 %50
            XPotion.Effect xPotion = XPotion.parseEffect(args);

            if(xPotion == null) return;

            Player target = ev.getWhoClicked();
            target.addPotionEffect(xPotion.getEffect());

        });

    }

    private GuiAction closeInventory() {
        return new GuiAction((ev, args) -> {

            Player target = ev.getWhoClicked();
            target.closeInventory();

        });
    }

}
