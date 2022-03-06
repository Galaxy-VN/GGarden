package io.github.galaxyvn.ggarden.command.framework.types;

import org.bukkit.entity.Player;

public class SelectorPlayer {

    private final Player value;

    public SelectorPlayer(Player value) {
        this.value = value;
    }

    /**
     * @return the Player value
     */
    public Player get() {
        return this.value;
    }

}
