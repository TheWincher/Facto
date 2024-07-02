package org.thewitcher.facto.block.pipe;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Connection implements StringRepresentable {
    CONNECTED(true),
    DISCONNECTED(false),
    BLOCKED(false),
    EXTERNAL(true);

    private final String name;
    private final boolean connected;

    Connection(boolean connected) {
        this.connected = connected;
        this.name = this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public boolean isConnected() {
        return this.connected;
    }
}
