package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class OPNetheriteHelmet {
    public static final ItemStack ITEM = new ItemStack(Material.NETHERITE_HELMET);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("OP Netherithelm", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(LowdFX.OP_LORE);
            meta.setUnbreakable(true);

            meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.armor"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET
            ));
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.armortoughness"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET));
            meta.addAttributeModifier(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.explosionknockbackresistance"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET));
        });
    }

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}