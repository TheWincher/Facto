package org.thewitcher.facto.item.modules;

public enum ModuleTier {
    MK_1,
    MK_2,
    MK_3;

    public final <T> T forTier(T low, T medium, T high) {
        return switch (this) {
            case MK_1 -> low;
            case MK_2 -> medium;
            case MK_3 -> high;
        };
    }
}
