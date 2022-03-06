package io.github.galaxyvn.ggarden.command;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.objects.GalaxyPluginData;
import io.github.galaxyvn.ggarden.utils.HexUtils;
import io.github.galaxyvn.ggarden.utils.GGardenUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class GgdCommand extends BukkitCommand {

    private final GalaxyPlugin galaxyPlugin;

    public GgdCommand(GalaxyPlugin galaxyPlugin) {
        super("ggd", "GGarden Developement information command", "ggd", Collections.emptyList());

        this.galaxyPlugin = galaxyPlugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.isOp()) {
            GGardenUtils.sendMessage(sender, "&cYou do not have permission to use this command.");
            return true;
        }

        List<GalaxyPluginData> pluginData = this.galaxyPlugin.getLoadedGalaxyPluginsData();

        ComponentBuilder builder = new ComponentBuilder();
        builder.append(TextComponent.fromLegacyText(HexUtils.colorify(
                GGardenUtils.PREFIX + "&ePlugins installed by " + GGardenUtils.GRADIENT + "GGarden Development&e. Hover over to view info: ")));

        boolean first = true;
        for (GalaxyPluginData data : pluginData) {
            if (!first)
                builder.append(TextComponent.fromLegacyText(HexUtils.colorify("&e, ")), FormatRetention.NONE);
            first = false;

            String updateVersion = data.getUpdateVersion();
            String website = data.getWebsite();

            List<Text> content = new ArrayList<>();
            content.add(new Text(TextComponent.fromLegacyText(HexUtils.colorify("&eVersion: &b" + data.getVersion()))));
            content.add(new Text(TextComponent.fromLegacyText(HexUtils.colorify("\n&eGGarden Version: &b" + data.getGalaxyGardenVersion()))));
            if (updateVersion != null)
                content.add(new Text(TextComponent.fromLegacyText(HexUtils.colorify("\n&eAn update (&bv" + updateVersion + "&e) is available! Click to open the Spigot page."))));

            TextComponent pluginName = new TextComponent(TextComponent.fromLegacyText(HexUtils.colorify(GGardenUtils.GRADIENT + data.getName())));
            pluginName.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, content.toArray(new Text[0])));

            if (website != null)
                pluginName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, data.getWebsite()));

            builder.append(pluginName);
        }

        sender.spigot().sendMessage(builder.create());

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias,String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

}
