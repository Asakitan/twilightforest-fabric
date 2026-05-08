package twilightforest.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(FlowerPotBlock.class)
public interface FlowerPotBlockInvoker {
	@Invoker("addPlant")
	void codex_twilight$addPlant(ResourceLocation id, Supplier<? extends Block> supplier);
}
