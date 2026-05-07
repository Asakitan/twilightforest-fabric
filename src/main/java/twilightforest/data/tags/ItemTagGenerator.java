package twilightforest.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import twilightforest.TwilightForestMod;

public final class ItemTagGenerator {
    public static final TagKey<Item> BOAR_TEMPT_ITEMS = create("boar_tempt_items");
    public static final TagKey<Item> DEER_TEMPT_ITEMS = create("deer_tempt_items");
    public static final TagKey<Item> RAVEN_TEMPT_ITEMS = create("raven_tempt_items");
    public static final TagKey<Item> TINY_BIRD_TEMPT_ITEMS = create("tiny_bird_tempt_items");
    public static final TagKey<Item> DWARF_RABBIT_TEMPT_ITEMS = create("dwarf_rabbit_tempt_items");
    public static final TagKey<Item> PENGUIN_TEMPT_ITEMS = create("penguin_tempt_items");
    public static final TagKey<Item> SQUIRREL_TEMPT_ITEMS = create("squirrel_tempt_items");
    public static final TagKey<Item> BANNED_UNCRAFTING_INGREDIENTS = create("banned_uncrafting_ingredients");
    public static final TagKey<Item> BANNED_UNCRAFTABLES = create("banned_uncraftables");
    public static final TagKey<Item> UNCRAFTING_IGNORES_COST = create("uncrafting_ignores_cost");
    public static final TagKey<Item> IMMUNE_TO_THORNS = create("immune_to_thorns");
    public static final TagKey<Item> SCEPTERS = create("scepters");
    public static final TagKey<Item> KOBOLD_PACIFICATION_BREADS = create("kobold_pacification_breads");
    public static final TagKey<Item> KNIGHTMETAL_INGOTS = common("ingots/knightmetal");
    public static final TagKey<Item> REPAIRS_IRONWOOD_TOOLS = create("repairs_ironwood_tools");
    public static final TagKey<Item> REPAIRS_STEELEAF_TOOLS = create("repairs_steeleaf_tools");
    public static final TagKey<Item> REPAIRS_KNIGHTMETAL_TOOLS = create("repairs_knightmetal_tools");
    public static final TagKey<Item> REPAIRS_FIERY_TOOLS = create("repairs_fiery_tools");
    public static final TagKey<Item> REPAIRS_GIANT_TOOLS = create("repairs_giant_tools");
    public static final TagKey<Item> REPAIRS_ICE_TOOLS = create("repairs_ice_tools");
    public static final TagKey<Item> TRAVELLERS_BELT_BLACKLISTED = create("travellers_belt_blacklisted");
    public static final TagKey<Item> EMPERORS_CLOTH_APPLICABLE = create("emperors_cloth_applicable");

    private ItemTagGenerator() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, TwilightForestMod.prefix(path));
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
