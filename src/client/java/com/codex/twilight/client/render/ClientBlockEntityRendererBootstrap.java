package com.codex.twilight.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.client.renderer.block.JarRenderer;
import twilightforest.init.TFBlockEntities;

/**
 * F2.5 — paired client BlockEntityRenderer registration.
 *
 * <p>Workflow for the project owner: as each upstream Twilight Forest BER
 * is translated from NeoForge to Fabric and dropped into
 * {@code src/tfjava-client/twilightforest/client/renderer/block/}, uncomment
 * the matching {@code BlockEntityRendererFactories.register(…)} line below.
 * The Gradle build then compiles the new file from the LGPL-licensed
 * derivative-work source dir alongside this bootstrap, so the mod jar ships
 * the BER as soon as the next {@code gradlew buildAndInstall} runs.
 *
 * <p>Until each line is enabled, the corresponding TF block entity falls
 * through to its legacy-disguised vanilla BER (chest, barrel, brewing stand,
 * candle, etc.). That's still functional — the block is visible and animated
 * for both vanilla and paired clients — just not pixel-identical to upstream.
 *
 * <p>Registration order is independent; pick whichever BER is cheapest to
 * port first. Suggested priority by visibility:
 * <ol>
 *   <li>{@code KEEPSAKE_CASKET} (player death-loot box, high-frequency)</li>
 *   <li>{@code BRAZIER} / {@code OMINOUS_CANDLE} (boss-room ambience)</li>
 *   <li>{@code MASON_JAR} / {@code AMBIENT_JAR} (utility blocks)</li>
 *   <li>{@code FIREFLY} / {@code CICADA} / {@code MOONWORM} (small ambient sprites)</li>
 *   <li>{@code TROPHY} / {@code SKULL_CHEST} / {@code TF_CHEST} (boss reward visuals)</li>
 *   <li>{@code DRYING_RACK} / {@code RED_THREAD} / {@code REACTOR_DEBRIS} (rare blocks)</li>
 *   <li>{@code SINISTER_SPAWNER} / {@code CANDELABRA} / {@code SKULL_CANDLE} (cosmetic)</li>
 * </ol>
 *
 * <p>Once a registration is enabled, you can verify the wire-up by:
 * <pre>
 *   ./gradlew compileClientJava   # confirms the upstream class compiles
 *   ./gradlew buildAndInstall     # rebuilds the mod jar
 *   # restart local server, /tp to a TF block, observe the rendering
 * </pre>
 */
public final class ClientBlockEntityRendererBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/client/ber");

    private ClientBlockEntityRendererBootstrap() {
    }

    public static void bootstrap() {
        BlockEntityRendererRegistry.register(TFBlockEntities.MASON_JAR, JarRenderer.MasonJarRenderer::new);

        // ─────────────────────────────────────────────────────────────────────
        // F2.5 BER registrations — uncomment per-line as upstream port lands.
        // Each commented call corresponds to a renderer file the project owner
        // ports from local/twilightforest-1.21.1-src/src/main/java/twilightforest/client/renderer/block/
        // into src/tfjava-client/twilightforest/client/renderer/block/.
        // Use `BlockEntityRendererFactories` from fabric-rendering-v1; if any
        // upstream renderer needs vanilla {@code BlockEntityRendererProvider.Context}
        // helpers, the same {@code ctx.getBlockEntityRenderDispatcher()} +
        // {@code ctx.getModelManager()} accessors apply unchanged.
        // ─────────────────────────────────────────────────────────────────────
        //
        // import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
        // import twilightforest.init.TFBlockEntities;
        // import twilightforest.client.renderer.block.KeepsakeCasketRenderer;
        // import twilightforest.client.renderer.block.BrazierRenderer;
        // ...
        //
        // BlockEntityRendererRegistry.register(TFBlockEntities.KEEPSAKE_CASKET.get(), KeepsakeCasketRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.BRAZIER.get(),         BrazierRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.MASON_JAR.get(),       JarRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.AMBIENT_JAR.get(),     JarRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.OMINOUS_CANDLE.get(),  OminousCandleRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.SKULL_CANDLE.get(),    SkullCandleRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.CANDELABRA.get(),      CandelabraRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.CICADA.get(),          CicadaRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.FIREFLY.get(),         FireflyRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.MOONWORM.get(),        MoonwormRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.DRYING_RACK.get(),     DryingRackRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.RED_THREAD.get(),      RedThreadRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.REACTOR_DEBRIS.get(),  ReactorDebrisRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.SINISTER_SPAWNER.get(),SinisterSpawnerRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.SKULL_CHEST.get(),     SkullChestRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.TF_CHEST.get(),        TFChestRenderer::new);
        // BlockEntityRendererRegistry.register(TFBlockEntities.TROPHY.get(),          TrophyRenderer::new);
        //
        // ─────────────────────────────────────────────────────────────────────

        LOGGER.debug("F2.5 BER scaffold loaded — uncomment registrations above as upstream ports land in src/tfjava-client/.");
    }
}
