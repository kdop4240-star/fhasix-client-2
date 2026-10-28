package com.fhasix.uitheme;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

/**
 * NEW. Block Overlay: replaces the vanilla black outline on the block you look at with a
 * customisable highlight. Options (see NewModOptions): Highlight = Border only / Whole block /
 * Both, border color + opacity, overlay color + opacity.
 *
 * NOTE FOR WHOEVER COMPILES THIS: world-rendering APIs changed a lot around 1.21.9+, and this
 * class was written without being able to compile it. Keep the BEHAVIOUR (draw the target block's
 * outline shape as filled boxes and/or line boxes, camera-relative, then cancel the vanilla
 * outline) but adapt the event / WorldRenderContext / VertexRendering / RenderLayer calls to
 * whatever 1.21.11 + this Fabric API actually provides. Only this file should need changes.
 */
final class NewBlockOverlay {

    static final String ID = "block_overlay";

    private NewBlockOverlay() {
    }

    static void init() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> NewSafe.call("block_overlay", () -> onOutline(context, hit), Boolean.TRUE));
    }

    /** Return true to let vanilla draw its outline, false to cancel it (we drew our own). */
    private static boolean onOutline(WorldRenderContext context, HitResult hit) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.world == null || !NewModSettings.isEnabled(c, ID)) {
            return true;
        }
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return true;
        }
        BlockPos pos = bhr.getBlockPos();
        BlockState state = c.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(c.world, pos);
        if (shape.isEmpty()) {
            return true;
        }

        int mode = NewModSettings.opt(c, ID, "mode"); // 0 border, 1 whole block, 2 both
        int border = NewModOptions.paletteColor(NewModSettings.opt(c, ID, "bcolor"));
        float borderA = NewModSettings.opt(c, ID, "balpha") / 100f;
        int fill = NewModOptions.paletteColor(NewModSettings.opt(c, ID, "fcolor"));
        float fillA = NewModSettings.opt(c, ID, "falpha") / 100f;

        MatrixStack ms = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        Vec3d cam = context.camera().getPos();
        double ox = pos.getX() - cam.x;
        double oy = pos.getY() - cam.y;
        double oz = pos.getZ() - cam.z;
        final double e = 0.002; // grow slightly so the overlay doesn't z-fight with the block faces

        if (mode != 0) {
            VertexConsumer vc = consumers.getBuffer(RenderLayer.getDebugFilledBox());
            float r = ((fill >> 16) & 255) / 255f;
            float g = ((fill >> 8) & 255) / 255f;
            float b = (fill & 255) / 255f;
            shape.forEachBox((x1, y1, z1, x2, y2, z2) -> VertexRendering.drawFilledBox(ms, vc,
                    ox + x1 - e, oy + y1 - e, oz + z1 - e, ox + x2 + e, oy + y2 + e, oz + z2 + e,
                    r, g, b, fillA));
        }
        if (mode != 1) {
            VertexConsumer vc = consumers.getBuffer(RenderLayer.getLines());
            float r = ((border >> 16) & 255) / 255f;
            float g = ((border >> 8) & 255) / 255f;
            float b = (border & 255) / 255f;
            shape.forEachBox((x1, y1, z1, x2, y2, z2) -> VertexRendering.drawBox(ms, vc,
                    ox + x1 - e, oy + y1 - e, oz + z1 - e, ox + x2 + e, oy + y2 + e, oz + z2 + e,
                    r, g, b, borderA));
        }
        return false;
    }
}
