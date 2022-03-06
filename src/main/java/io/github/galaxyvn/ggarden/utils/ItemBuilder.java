package io.github.galaxyvn.ggarden.utils;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.cryptomorin.xseries.XMaterial.supports;

public final class ItemBuilder {

    private Material material;
    private int amount = 1;
    private int damage = 0;
    private String skull;
    private String color;
    private String variantStr;
    private String creatureName;
    private String name;
    private boolean unbreakable;
    private int modelData = -1;
    private List<String> lores;
    private List<String> flags;

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder(XMaterial material) {
        this.material = material.parseMaterial();
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public ItemBuilder setSkull(String skull) {
        this.skull = skull;
        return this;
    }

    public ItemBuilder setColor(String color) {
        this.color = color;
        return this;
    }

    public ItemBuilder setVariant(String variantStr) {
        this.variantStr = variantStr;
        return this;
    }

    public ItemBuilder setCreature(String creatureName) {
        this.creatureName = creatureName;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder setModelData(int modelData) {
        this.modelData = modelData;
        return this;
    }

    public ItemBuilder setLores(String... lore) {
        this.flags.addAll(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setFlags(String... flag) {
        this.flags.addAll(Arrays.asList(flag));
        return this;
    }

    public ItemStack build() {

        // Material
        ItemStack item = XMaterial.matchXMaterial(material).parseItem();
        if (item == null) return null;

        // Amount
        if (amount > 1) item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Durability - Damage
        if (supports(13)) {
            if (meta instanceof Damageable) {
                if (damage > 0) ((Damageable) meta).setDamage(damage);
            } else {
                if (damage > 0) item.setDurability((short) damage);
            }
        }

        // Special Items
        if (meta instanceof SkullMeta) {
            if (skull != null) SkullUtils.applySkin(meta, skull);
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            // TODO: Banner Meta
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            if (color != null) leather.setColor(parseColor(color));
        } else if (meta instanceof PotionMeta) {
            // TODO: Potion Meta
        } else if (supports(17)) {
            if (meta instanceof AxolotlBucketMeta) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                if (variantStr != null) {
                    Axolotl.Variant variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr.toUpperCase(Locale.ENGLISH)).or(Axolotl.Variant.BLUE);
                    bucket.setVariant(variant);
                }
            }
        } else if (!supports(13)) {
            // Spawn Eggs
            if (supports(11)) {
                if (meta instanceof SpawnEggMeta) {
                    if (!Strings.isNullOrEmpty(creatureName)) {
                        SpawnEggMeta spawnEgg = (SpawnEggMeta) meta;
                        com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
                        if (creature.isPresent()) spawnEgg.setSpawnedType(creature.get());
                    }
                }
            } else {
                MaterialData data = item.getData();
                if (data instanceof SpawnEgg) {
                    if (!Strings.isNullOrEmpty(creatureName)) {
                        SpawnEgg spawnEgg = (SpawnEgg) data;
                        com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
                        if (creature.isPresent()) spawnEgg.setSpawnedType(creature.get());
                        item.setData(data);
                    }
                }
            }
        }
        // TODO: Implement alot of meta

        // Display Name
        if (!Strings.isNullOrEmpty(name)) {
            String translated = ChatColor.translateAlternateColorCodes('&', name);
            meta.setDisplayName(translated);
        } else if (name != null && name.isEmpty()) meta.setDisplayName(" "); // For GUI easy access configuration purposes

        // Unbreakable
        if (supports(11)) meta.setUnbreakable(unbreakable);

        // Custom Model Data
        if (supports(14)) {
            if (modelData != 0) meta.setCustomModelData(modelData);
        }

        // Lore
        if (!lores.isEmpty()) {
            List<String> translatedLore = new ArrayList<>(lores.size());
            String lastColors = "";

            for (String lore : lores) {
                if (lore.isEmpty()) {
                    translatedLore.add(" ");
                    continue;
                }

                for (String singleLore : StringUtils.splitPreserveAllTokens(lore, '\n')) {
                    if (singleLore.isEmpty()) {
                        translatedLore.add(" ");
                        continue;
                    }
                    singleLore = lastColors + ChatColor.translateAlternateColorCodes('&', singleLore);
                    translatedLore.add(singleLore);

                    lastColors = ChatColor.getLastColors(singleLore);
                }
            }

            meta.setLore(translatedLore);
        }

        // Flags
        for (String flag : flags) {
            flag = flag.toUpperCase(Locale.ENGLISH);
            if (flag.equals("ALL")) {
                meta.addItemFlags(ItemFlag.values());
                break;
            }

            ItemFlag itemFlag = Enums.getIfPresent(ItemFlag.class, flag).orNull();
            if (itemFlag != null) meta.addItemFlags(itemFlag);
        }

        item.setItemMeta(meta);
        return item;
    }

    @Nonnull
    public static Color parseColor(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return Color.BLACK;
        String[] rgb = StringUtils.split(StringUtils.deleteWhitespace(str), ',');
        if (rgb.length < 3) return Color.WHITE;
        return Color.fromRGB(NumberUtils.toInt(rgb[0], 0), NumberUtils.toInt(rgb[1], 0), NumberUtils.toInt(rgb[2], 0));
    }
}
