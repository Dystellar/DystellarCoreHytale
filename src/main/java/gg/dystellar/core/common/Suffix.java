package gg.dystellar.core.common;

import javax.annotation.Nullable;

/**
 * Suffixes, used for cosmetics. Should probably implement a proper suffix system that integrates with the backend instead of this hardcoded shit.
 */
public enum Suffix {

    NONE("", 10, null),
    L("&b【&c&lL&b】", 11, "dystellar.suffix.l"),
    LOL("&b【&c&lLOL&b】", 12, "dystellar.suffix.lol"),
    GG("&b【&6&lGG&b】", 13, "dystellar.suffix.gg"),
    GF("&b【&6&lGF&b】", 14, "dystellar.suffix.gf"),
    LMAO("&b【&1&lLMAO&b】", 15, "dystellar.suffix.lmao"),
    LMFAO("&b【&1&lLMFAO&b】", 16, "dystellar.suffix.lmfao"),
    HEART("&8[&c&l<3&8]", 17, "dystellar.suffix.heart"),
    USA("&1░&cU&fS&cA&1░", 20, "dystellar.suffix.usa"),
    COL("&eC&1O&cL", 21, "dystellar.suffix.col"),
    VEN("&f☆&eV&1E&cN&f☆", 22, "dystellar.suffix.ven"),
    ARG("&e✺&bA&fR&bG&e✺", 23, "dystellar.suffix.arg"),
    URU("&e✺&9U&fR&9U&e✺", 24, "dystellar.suffix.uru"),
    ESP("&cE&eS&cP", 25, "dystellar.suffix.esp"),
    PA("&1☆P&cA☆", 26, "dystellar.suffix.pa"),
    UK("&cU&1K", 29, "dystellar.suffix.uk"),
    IT("&aI&fT&cA", 30, "dystellar.suffix.it"),
    MEX("&2M&fE&cX", 31, "dystellar.suffix.mex"),
    BR("&aB&eR&1A", 32, "dystellar.suffix.br"),
    PE("&cP&fE&cR", 33, "dystellar.suffix.pe"),
    BOL("&cB&eO&2L", 34, "dystellar.suffix.bol"),
    CUB("&cC&1U&fB", 35, "dystellar.suffix.cub"),
    CHL("&1C&fH&cL", 38, "dystellar.suffix.chl"),
    FR("&1F&fR&cA", 39, "dystellar.suffix.fr"),
    GER("&0G&cE&eR", 40, "dystellar.suffix.ger"),
    POL("&fP&cO&fL", 41, "dystellar.suffix.pol"),
    IR("&2I&fR&eE", 42, "dystellar.suffix.ir"),
    ROM("&1R&eO&cM", 43, "dystellar.suffix.rom"),
    TR("&f☾&cTR&f☆", 44, "dystellar.suffix.tr");


    Suffix(String suffix, int slot, @Nullable String permission) {
        this.suffix = suffix;
        this.permission = permission;
    }

    private final String suffix;
    @Nullable private final String permission;

    @Nullable
    public String getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return suffix;
    }
}
