# FHASIX 1.21.11 source audit status

Target configuration retained: Minecraft 1.21.11, Yarn 1.21.11+build.4, Fabric Loader 0.18.4, Fabric API 0.141.5+1.21.11, Java 21.

## Applied compatibility edits
- Corrected Fabric client event imports to `net.fabricmc.fabric.api.client.event.lifecycle.v1`.
- Updated screen key callback type from `KeyEvent` to `KeyInput`.
- Updated particle option enum references from `ParticlesMode` to `ParticleStatus`.
- Updated graphics preset option access to `GameOptions.getPreset()`.
- Removed obsolete `AbstractTexture.setFilter(...)` calls, which are no longer present in 1.21.11's GPU-backed texture API.
- Updated skin texture lookup to `PlayerSkinProvider.supplySkinTextures(...)` and constructed `GameProfile` from the current session values.
- Removed obsolete entity previous-rotation field assignments.
- Updated custom button drawing hooks for 1.21.11's final `PressableWidget.renderWidget` and abstract `drawIcon` API.

## Important unresolved blockers
This is **not a verified successful build**. The supplied GitHub log stops after javac's 100-error limit. At minimum, the custom world-rendering implementations in `NewBlockOverlay.java` and `NewOreHighlight.java` need a proper 1.21.11 render-state/GPU pipeline port: their old `WorldRenderContext`, `RenderLayer`, and `VertexRendering` calls do not match the target APIs. The full project must be rebuilt in an environment that can resolve Fabric/Minecraft dependencies before it can be called fixed.

No game features or resource assets were intentionally removed by this patch. The original resource-pack and texture files are included.
