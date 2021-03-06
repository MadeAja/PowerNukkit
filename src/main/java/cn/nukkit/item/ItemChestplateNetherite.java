package cn.nukkit.item;

import cn.nukkit.api.Since;

@Since("1.3.2.0-PN")
public class ItemChestplateNetherite extends ItemArmor {

    @Since("1.3.2.0-PN")
    public ItemChestplateNetherite() {
        this(0, 1);
    }

    @Since("1.3.2.0-PN")
    public ItemChestplateNetherite(Integer meta) {
        this(meta, 1);
    }

    @Since("1.3.2.0-PN")
    public ItemChestplateNetherite(Integer meta, int count) {
        super(NETHERITE_CHESTPLATE, meta, count, "Netherite Chestplate");
    }

    @Override
    public boolean isChestplate() {
        return true;
    }

    @Override
    public int getTier() {
        return ItemArmor.TIER_NETHERITE;
    }

    @Override
    public int getMaxDurability() {
        return 592;
    }

    @Override
    public int getArmorPoints() {
        return 8;
    }

    @Override
    public int getToughness() {
        return 2;
    }
}
