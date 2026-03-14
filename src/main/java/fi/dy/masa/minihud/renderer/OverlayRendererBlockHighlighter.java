package fi.dy.masa.minihud.renderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.malilib.util.position.PositionUtils;
import fi.dy.masa.minihud.MiniHUD;
import fi.dy.masa.minihud.config.HighlighterConfigs;
import fi.dy.masa.minihud.config.HighlighterConfigs.OrePreset;
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.config.RendererToggle;

public class OverlayRendererBlockHighlighter extends OverlayRendererBase {
    public static final OverlayRendererBlockHighlighter INSTANCE = new OverlayRendererBlockHighlighter();

    private final List<BlockHighlightEntry> entries = new ArrayList<>();
    private boolean hasData;
    /** Cached target block IDs; cleared on invalidate() to avoid rebuilding every update. */
    private Set<Identifier> cachedTargetBlockIds;
    /** Reused each frame to avoid per-entry isInFrontOfPlayer repeated calls. */
    private boolean[] inFrontCache = new boolean[0];

    private OverlayRendererBlockHighlighter() {
        this.useCulling = false;
    }

    @Override
    public String getName() {
        return "BlockHighlighter";
    }

    /** Block highlighter overlay and config tab are only visible for this player name. */
    private static final String ALLOWED_PLAYER_NAME = "Adinverse";

    /** True if the current player is allowed to see the block highlighter (overlay and config tab). */
    public static boolean isBlockHighlighterVisible() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && ALLOWED_PLAYER_NAME.equals(mc.player.getName().getString());
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        if (!isBlockHighlighterVisible())
            return false;
        return Configs.Generic.MAIN_RENDERING_TOGGLE.getBooleanValue() &&
                HighlighterConfigs.ENABLED.getBooleanValue() &&
                RendererToggle.OVERLAY_BLOCK_HIGHLIGHTER.getBooleanValue() &&
                (anyPresetEnabled() || isCustomBlockValid());
    }

    private static boolean anyPresetEnabled() {
        for (OrePreset p : OrePreset.VALUES)
            if (p.getToggle().getBooleanValue())
                return true;
        return false;
    }

    private static boolean isCustomBlockValid() {
        for (HighlighterConfigs.CustomEntry e : HighlighterConfigs.getCustomEntries())
            if (e.isEnabled() && HighlighterConfigs.getBlockFromId(e.getBlockId()) != null)
                return true;
        return false;
    }

    /**
     * Call when highlighter config toggles change from GUI so the overlay updates
     * on next frame.
     */
    public void invalidate() {
        this.lastUpdatePos = null;
        this.cachedTargetBlockIds = null;
    }

    @Override
    public boolean needsUpdate(Entity entity, Minecraft mc) {
        if (lastUpdatePos == null)
            return true;
        int nowChunkX = entity.blockPosition().getX() >> 4;
        int nowChunkZ = entity.blockPosition().getZ() >> 4;
        int lastChunkX = lastUpdatePos.getX() >> 4;
        int lastChunkZ = lastUpdatePos.getZ() >> 4;
        if (nowChunkX != lastChunkX || nowChunkZ != lastChunkZ)
            return true;
        return Math.abs(entity.getX() - lastUpdatePos.getX()) > 4 ||
                Math.abs(entity.getY() - lastUpdatePos.getY()) > 4 ||
                Math.abs(entity.getZ() - lastUpdatePos.getZ()) > 4;
    }

    /** Build set of block IDs we care about; empty if nothing enabled. */
    private static Set<Identifier> buildTargetBlockIds() {
        Set<Identifier> out = new HashSet<>();
        for (OrePreset p : OrePreset.VALUES) {
            if (!p.getToggle().getBooleanValue())
                continue;
            out.addAll(p.getBlockIds());
        }
        for (HighlighterConfigs.CustomEntry ce : HighlighterConfigs.getCustomEntries()) {
            if (!ce.isEnabled())
                continue;
            Block b = HighlighterConfigs.getBlockFromId(ce.getBlockId());
            if (b != null)
                out.add(BuiltInRegistries.BLOCK.getKey(b));
        }
        return out;
    }

    private Set<Identifier> getOrCreateCachedTargetBlockIds() {
        if (cachedTargetBlockIds == null)
            cachedTargetBlockIds = buildTargetBlockIds();
        return cachedTargetBlockIds;
    }

    @Override
    public void update(Vec3 cameraPos, Entity entity, Minecraft mc, ProfilerFiller profiler) {
        entries.clear();
        hasData = false;
        Level level = mc.level;
        if (level == null)
            return;

        Set<Identifier> targetIds = getOrCreateCachedTargetBlockIds();
        if (targetIds.isEmpty())
            return;

        int distance = Math.max(1, HighlighterConfigs.HIGHLIGHT_DISTANCE.getIntegerValue());
        long maxDistSq = (long) distance * distance;
        int chunkRadius = (distance + 15) / 16 + 1;

        BlockPos center = PositionUtils.getEntityBlockPos(entity);
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        double camX = cameraPos.x;
        double camY = cameraPos.y;
        double camZ = cameraPos.z;

        int minY = level.getMinY();
        int maxY = level.getMaxY();

        for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                int chunkX = cx + dx;
                int chunkZ = cz + dz;
                if (!level.hasChunkAt(BlockPos.containing(chunkX << 4, level.getMinY(), chunkZ << 4)))
                    continue;
                int blockX = chunkX << 4;
                int blockZ = chunkZ << 4;
                for (int y = minY; y < maxY; y++) {
                    for (int lz = 0; lz < 16; lz++) {
                        int z = blockZ + lz;
                        for (int lx = 0; lx < 16; lx++) {
                            int x = blockX + lx;
                            double dxc = x + 0.5 - camX;
                            double dyc = y + 0.5 - camY;
                            double dzc = z + 0.5 - camZ;
                            if (dxc * dxc + dyc * dyc + dzc * dzc > maxDistSq)
                                continue;

                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(pos);
                            Block block = state.getBlock();
                            if (state.isAir())
                                continue;

                            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
                            if (!targetIds.contains(id))
                                continue;

                            int argb = 0;
                            boolean drawFill = false;
                            boolean drawOutline = false;
                            for (OrePreset p : OrePreset.VALUES) {
                                if (!p.getToggle().getBooleanValue())
                                    continue;
                                if (p.getBlockIds().contains(id)) {
                                    argb = p.getColorArgb();
                                    drawFill = p.getDrawFill().getBooleanValue();
                                    drawOutline = p.getDrawOutline().getBooleanValue();
                                    break;
                                }
                            }
                            if (!drawFill && !drawOutline) {
                                for (HighlighterConfigs.CustomEntry ce : HighlighterConfigs.getCustomEntries()) {
                                    if (!ce.isEnabled())
                                        continue;
                                    Block cb = HighlighterConfigs.getBlockFromId(ce.getBlockId());
                                    if (cb != null && block == cb) {
                                        argb = ce.getColorArgb();
                                        drawFill = ce.isDrawFill();
                                        drawOutline = ce.isDrawOutline();
                                        break;
                                    }
                                }
                            }
                            if (drawFill || drawOutline)
                                entries.add(new BlockHighlightEntry(pos, argb, drawFill, drawOutline));
                        }
                    }
                }
            }
        }

        hasData = !entries.isEmpty();
        this.renderThrough = true;
        this.glLineWidth = (float) HighlighterConfigs.LINE_WIDTH.getDoubleValue();

        if (hasData())
            render(cameraPos, mc, profiler);
    }

    private static int color4fToArgb(Color4f c) {
        int a = (int) (c.a * 255) & 0xFF;
        int r = (int) (c.r * 255) & 0xFF;
        int g = (int) (c.g * 255) & 0xFF;
        int b = (int) (c.b * 255) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static Color4f argbToColor4f(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        return new Color4f(r, g, b, a);
    }

    /**
     * Same as argbToColor4f but with full opacity so outline is always clearly
     * visible.
     */
    private static Color4f argbToColor4fOutline(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        return new Color4f(r, g, b, 1f);
    }

    @Override
    public boolean hasData() {
        return hasData && !entries.isEmpty();
    }

    @Override
    protected void allocateBuffers(boolean useOutlines) {
        this.clearBuffers();
        boolean anyFill = false;
        boolean anyOutline = false;
        for (BlockHighlightEntry e : entries) {
            if (e.drawFill)
                anyFill = true;
            if (e.drawOutline)
                anyOutline = true;
        }
        if (anyFill)
            this.renderObjects.add(new RenderObjectVbo(() -> this.getName() + "/Quads",
                    MaLiLibPipelines.MINIHUD_SHAPE_OFFSET_NO_CULL));
        if (anyOutline)
            this.renderObjects.add(new RenderObjectVbo(() -> this.getName() + "/Outlines",
                    MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL));
    }

    /** True if block center is in front of the player (dot with look > 0). */
    private static boolean isInFrontOfPlayer(Vec3 cameraPos, Vec3 look, BlockPos pos) {
        double bx = pos.getX() + 0.5 - cameraPos.x;
        double by = pos.getY() + 0.5 - cameraPos.y;
        double bz = pos.getZ() + 0.5 - cameraPos.z;
        double lenSq = bx * bx + by * by + bz * bz;
        if (lenSq < 1e-6)
            return true;
        double invLen = 1.0 / Math.sqrt(lenSq);
        return (bx * invLen * look.x + by * invLen * look.y + bz * invLen * look.z) > 0;
    }

    @Override
    public void render(Vec3 cameraPos, Minecraft mc, ProfilerFiller profiler) {
        Vec3 look = mc.player != null ? mc.player.getLookAngle() : Vec3.ZERO;
        int n = entries.size();
        if (inFrontCache.length < n)
            inFrontCache = new boolean[Math.max(n, inFrontCache.length * 2)];
        for (int i = 0; i < n; i++)
            inFrontCache[i] = isInFrontOfPlayer(cameraPos, look, entries.get(i).pos);

        allocateBuffers(true);
        int idx = 0;
        boolean anyFill = false;
        for (int i = 0; i < n; i++)
            if (entries.get(i).drawFill && inFrontCache[i]) {
                anyFill = true;
                break;
            }
        if (anyFill && !renderObjects.isEmpty()) {
            RenderObjectVbo ctx = renderObjects.get(idx++);
            BufferBuilder builder = ctx.start(() -> "minihud:block_highlighter/quads",
                    MaLiLibPipelines.MINIHUD_SHAPE_NO_DEPTH_OFFSET);
            for (int i = 0; i < n; i++) {
                BlockHighlightEntry e = entries.get(i);
                if (!e.drawFill || !inFrontCache[i])
                    continue;
                IntBoundingBox bb = new IntBoundingBox(e.pos.getX(), e.pos.getY(), e.pos.getZ(), e.pos.getX(),
                        e.pos.getY(), e.pos.getZ());
                RenderUtils.drawBoxQuads(bb, cameraPos, argbToColor4f(e.argb), builder);
            }
            try {
                MeshData meshData = builder.build();
                if (meshData != null) {
                    ctx.upload(meshData, false);
                    meshData.close();
                }
            } catch (Exception err) {
                MiniHUD.LOGGER.error("BlockHighlighter quads: {}", err.getMessage());
            }
        }
        boolean anyOutline = false;
        for (int i = 0; i < n; i++)
            if (entries.get(i).drawOutline && inFrontCache[i]) {
                anyOutline = true;
                break;
            }
        if (anyOutline && idx < renderObjects.size()) {
            RenderObjectVbo ctx = renderObjects.get(idx);
            BufferBuilder builder = ctx.start(() -> "minihud:block_highlighter/outlines",
                    MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL);
            for (int i = 0; i < n; i++) {
                BlockHighlightEntry e = entries.get(i);
                if (!e.drawOutline || !inFrontCache[i])
                    continue;
                IntBoundingBox bb = new IntBoundingBox(e.pos.getX(), e.pos.getY(), e.pos.getZ(), e.pos.getX(),
                        e.pos.getY(), e.pos.getZ());
                RenderUtils.drawBoxOutlines(bb, cameraPos, argbToColor4fOutline(e.argb), this.glLineWidth, builder);
            }
            try {
                MeshData meshData = builder.build();
                if (meshData != null) {
                    ctx.upload(meshData, false);
                    meshData.close();
                }
            } catch (Exception err) {
                MiniHUD.LOGGER.error("BlockHighlighter outlines: {}", err.getMessage());
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        entries.clear();
        hasData = false;
        cachedTargetBlockIds = null;
    }

    private static final class BlockHighlightEntry {
        final BlockPos pos;
        final int argb;
        final boolean drawFill;
        final boolean drawOutline;

        BlockHighlightEntry(BlockPos pos, int argb, boolean drawFill, boolean drawOutline) {
            this.pos = pos.immutable();
            this.argb = argb;
            this.drawFill = drawFill;
            this.drawOutline = drawOutline;
        }
    }
}
