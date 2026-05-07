package twilightforest.data.tags;

import net.minecraft.core.registries.Registries;
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

    private ItemTagGenerator() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, TwilightForestMod.prefix(path));
    }
}
