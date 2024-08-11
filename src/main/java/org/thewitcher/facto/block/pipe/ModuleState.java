package org.thewitcher.facto.block.pipe;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ModuleState implements StringRepresentable {
    NONE(),
    EXTRACT(),
    CALL(),
    STORAGE();

    private final String name;

    ModuleState() {
        this.name = this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
