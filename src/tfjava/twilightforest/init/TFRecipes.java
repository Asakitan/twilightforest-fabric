package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import twilightforest.TwilightForestMod;
import twilightforest.item.recipe.DryingRecipe;
import twilightforest.item.recipe.UncraftingRecipe;

/**
 * Codex Fabric port of upstream {@code twilightforest.init.TFRecipes}.
 * Currently registers {@link DryingRecipe} type + serializer so the
 * drying-rack tick loop can resolve recipes loaded from JSON datapacks.
 *
 * <p>Bootstrap is class-init driven (calling {@link #bootstrap()} once forces
 * both static fields to resolve, which performs the registry writes through
 * vanilla's {@link Registry#register} hook).</p>
 */
public final class TFRecipes {

	public static final RecipeType<DryingRecipe> DRYING_RECIPE = Registry.register(
		BuiltInRegistries.RECIPE_TYPE,
		TwilightForestMod.prefix("drying"),
		new RecipeType<>() {
			@Override
			public String toString() { return "twilightforest:drying"; }
		});

	public static final RecipeSerializer<DryingRecipe> DRYING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER,
		TwilightForestMod.prefix("drying"),
		new DryingRecipe.Serializer());

	public static final RecipeType<UncraftingRecipe> UNCRAFTING_RECIPE = Registry.register(
		BuiltInRegistries.RECIPE_TYPE,
		TwilightForestMod.prefix("uncrafting"),
		new RecipeType<>() {
			@Override
			public String toString() { return "twilightforest:uncrafting"; }
		});

	public static final RecipeSerializer<UncraftingRecipe> UNCRAFTING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER,
		TwilightForestMod.prefix("uncrafting"),
		new UncraftingRecipe.Serializer());

	private TFRecipes() {}

	public static void bootstrap() {
		// Class-init triggers registration; this method just ensures the class is loaded.
	}
}
