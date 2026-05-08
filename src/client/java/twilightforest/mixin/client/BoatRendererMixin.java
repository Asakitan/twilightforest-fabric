package twilightforest.mixin.client;

import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BoatRenderer.class)
public abstract class BoatRendererMixin {
	@Redirect(
		method = "getTextureLocation(Lnet/minecraft/world/entity/vehicle/Boat$Type;Z)Lnet/minecraft/resources/ResourceLocation;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;")
	)
	private static ResourceLocation codex_twilight$useNamespacedBoatTexture(String path) {
		int slash = path.lastIndexOf('/');
		int colon = path.indexOf(':', slash + 1);
		if (colon > slash) {
			String namespace = path.substring(slash + 1, colon);
			String namespacedPath = path.substring(0, slash + 1) + path.substring(colon + 1);
			return ResourceLocation.fromNamespaceAndPath(namespace, namespacedPath);
		}
		return ResourceLocation.withDefaultNamespace(path);
	}
}
