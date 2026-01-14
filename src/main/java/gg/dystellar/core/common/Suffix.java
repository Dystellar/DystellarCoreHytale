package gg.dystellar.core.common;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;

import static gg.zylesh.dystellarcore.DystellarCore.NULL_GLASS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        this.slot = slot;
        this.permission = permission;
    }

    private ItemStack icon;
    private final int slot;
    private final String suffix;
    @Nullable private final String permission;

    @Nullable
    public String getPermission() {
        return permission;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return suffix;
    }

    public static void initialize() {
        List<String> lore = List.of(
                " ",
                ChatColor.YELLOW + "Click to select."
        );
        ItemStack NONE_ITEM = new ItemStack(Material.COAL_BLOCK);
        ItemMeta none = NONE_ITEM.getItemMeta();
        none.setDisplayName(NONE.suffix);
        none.setLore(lore);
        NONE_ITEM.setItemMeta(none);
        NONE.setIcon(NONE_ITEM);
        ItemStack L_ITEM = new ItemStack(Material.WOOL);
        ItemMeta l = L_ITEM.getItemMeta();
        l.setDisplayName(L.suffix);
        l.setLore(lore);
        L_ITEM.setItemMeta(l);
        L.setIcon(L_ITEM);
        ItemStack LOL_ITEM = new ItemStack(Material.WOOL);
        ItemMeta lol = LOL_ITEM.getItemMeta();
        lol.setDisplayName(LOL.suffix);
        lol.setLore(lore);
        LOL_ITEM.setItemMeta(lol);
        LOL.setIcon(LOL_ITEM);
        ItemStack GG_ITEM = new ItemStack(Material.WOOL);
        ItemMeta gg = GG_ITEM.getItemMeta();
        gg.setDisplayName(GG.suffix);
        gg.setLore(lore);
        GG_ITEM.setItemMeta(gg);
        GG.setIcon(GG_ITEM);
        ItemStack GF_ITEM = new ItemStack(Material.WOOL);
        ItemMeta gf = GF_ITEM.getItemMeta();
        gf.setDisplayName(GF.suffix);
        gf.setLore(lore);
        GF_ITEM.setItemMeta(gf);
        GF.setIcon(GF_ITEM);
        ItemStack LMAO_ITEM = new ItemStack(Material.WOOL);
        ItemMeta lmao = LMAO_ITEM.getItemMeta();
        lmao.setDisplayName(LMAO.suffix);
        lmao.setLore(lore);
        LMAO_ITEM.setItemMeta(lmao);
        LMAO.setIcon(LMAO_ITEM);
        ItemStack LMFAO_ITEM = new ItemStack(Material.WOOL);
        ItemMeta lmfao = LMFAO_ITEM.getItemMeta();
        lmfao.setDisplayName(LMFAO.suffix);
        lmfao.setLore(lore);
        LMFAO_ITEM.setItemMeta(lmfao);
        LMFAO.setIcon(LMFAO_ITEM);
        ItemStack HEART_ITEM = new ItemStack(Material.WOOL);
        ItemMeta heart = HEART_ITEM.getItemMeta();
        heart.setDisplayName(HEART.suffix);
        heart.setLore(lore);
        HEART_ITEM.setItemMeta(heart);
        HEART.setIcon(HEART_ITEM);
        ItemStack USA_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta usa = USA_ITEM.getItemMeta();
        usa.setDisplayName(USA.suffix);
        usa.setLore(lore);
        USA_ITEM.setItemMeta(usa);
        USA.setIcon(USA_ITEM);
        ItemStack COL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta col = COL_ITEM.getItemMeta();
        col.setDisplayName(COL.suffix);
        col.setLore(lore);
        COL_ITEM.setItemMeta(col);
        COL.setIcon(COL_ITEM);
        ItemStack VEN_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ven = VEN_ITEM.getItemMeta();
        ven.setDisplayName(VEN.suffix);
        ven.setLore(lore);
        VEN_ITEM.setItemMeta(ven);
        VEN.setIcon(VEN_ITEM);
        ItemStack ARG_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta arg = ARG_ITEM.getItemMeta();
        arg.setDisplayName(ARG.suffix);
        arg.setLore(lore);
        ARG_ITEM.setItemMeta(arg);
        ARG.setIcon(ARG_ITEM);
        ItemStack URU_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta uru = URU_ITEM.getItemMeta();
        uru.setDisplayName(URU.suffix);
        uru.setLore(lore);
        URU_ITEM.setItemMeta(uru);
        URU.setIcon(URU_ITEM);
        ItemStack ESP_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta esp = ESP_ITEM.getItemMeta();
        esp.setDisplayName(ESP.suffix);
        esp.setLore(lore);
        ESP_ITEM.setItemMeta(esp);
        ESP.setIcon(ESP_ITEM);
        ItemStack PA_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta pa = PA_ITEM.getItemMeta();
        pa.setDisplayName(PA.suffix);
        pa.setLore(lore);
        PA_ITEM.setItemMeta(pa);
        PA.setIcon(PA_ITEM);
        ItemStack UK_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta uk = UK_ITEM.getItemMeta();
        uk.setDisplayName(UK.suffix);
        uk.setLore(lore);
        UK_ITEM.setItemMeta(uk);
        UK.setIcon(UK_ITEM);
        ItemStack IT_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta it = IT_ITEM.getItemMeta();
        it.setDisplayName(IT.suffix);
        it.setLore(lore);
        IT_ITEM.setItemMeta(it);
        IT.setIcon(IT_ITEM);
        ItemStack MEX_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta mex = MEX_ITEM.getItemMeta();
        mex.setDisplayName(MEX.suffix);
        mex.setLore(lore);
        MEX_ITEM.setItemMeta(mex);
        MEX.setIcon(MEX_ITEM);
        ItemStack BR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta br = BR_ITEM.getItemMeta();
        br.setDisplayName(BR.suffix);
        br.setLore(lore);
        BR_ITEM.setItemMeta(br);
        BR.setIcon(BR_ITEM);
        ItemStack PE_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta pe = PE_ITEM.getItemMeta();
        pe.setDisplayName(PE.suffix);
        pe.setLore(lore);
        PE_ITEM.setItemMeta(pe);
        PE.setIcon(PE_ITEM);
        ItemStack BOL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta bol = BOL_ITEM.getItemMeta();
        bol.setDisplayName(BOL.suffix);
        bol.setLore(lore);
        BOL_ITEM.setItemMeta(bol);
        BOL.setIcon(BOL_ITEM);
        ItemStack CUB_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta cub = CUB_ITEM.getItemMeta();
        cub.setDisplayName(CUB.suffix);
        cub.setLore(lore);
        CUB_ITEM.setItemMeta(cub);
        CUB.setIcon(CUB_ITEM);
        ItemStack CHL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta chl = CHL_ITEM.getItemMeta();
        chl.setDisplayName(CHL.suffix);
        chl.setLore(lore);
        CHL_ITEM.setItemMeta(chl);
        CHL.setIcon(CHL_ITEM);
        ItemStack FR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta fr = FR_ITEM.getItemMeta();
        fr.setDisplayName(FR.suffix);
        fr.setLore(lore);
        FR_ITEM.setItemMeta(fr);
        FR.setIcon(FR_ITEM);
        ItemStack GER_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ger = GER_ITEM.getItemMeta();
        ger.setDisplayName(GER.suffix);
        ger.setLore(lore);
        GER_ITEM.setItemMeta(ger);
        GER.setIcon(GER_ITEM);
        ItemStack POL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta pol = POL_ITEM.getItemMeta();
        pol.setDisplayName(POL.suffix);
        pol.setLore(lore);
        POL_ITEM.setItemMeta(pol);
        POL.setIcon(POL_ITEM);
        ItemStack IR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ir = IR_ITEM.getItemMeta();
        ir.setDisplayName(IR.suffix);
        ir.setLore(lore);
        IR_ITEM.setItemMeta(ir);
        IR.setIcon(IR_ITEM);
        ItemStack ROM_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta rom = ROM_ITEM.getItemMeta();
        rom.setDisplayName(ROM.suffix);
        rom.setLore(lore);
        ROM_ITEM.setItemMeta(rom);
        ROM.setIcon(ROM_ITEM);
        ItemStack TR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta tr = TR_ITEM.getItemMeta();
        tr.setDisplayName(TR.suffix);
        tr.setLore(lore);
        TR_ITEM.setItemMeta(tr);
        TR.setIcon(TR_ITEM);
        for (int i = 0; i < 10; i++) {
            GUI.setItem(i, NULL_GLASS);
        }
        GUI.setItem(17, NULL_GLASS);
        GUI.setItem(18, NULL_GLASS);
        GUI.setItem(26, NULL_GLASS);
        GUI.setItem(27, NULL_GLASS);
        GUI.setItem(35, NULL_GLASS);
        GUI.setItem(36, NULL_GLASS);
        for (int i = 44; i < 54; i++) {
            GUI.setItem(i, NULL_GLASS);
        }
        for (Suffix suffix1 : values()) {
            SUFFIXES_BY_SLOT.put(suffix1.slot, suffix1);
            GUI.setItem(suffix1.slot, suffix1.icon);
        }
    }

    public static final Map<Integer, Suffix> SUFFIXES_BY_SLOT = new HashMap<>();
    public static final Inventory GUI = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "Suffixes");
}
