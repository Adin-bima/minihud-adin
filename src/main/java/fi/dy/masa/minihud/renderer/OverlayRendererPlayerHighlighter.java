package fi.dy.masa.minihud.renderer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.minihud.MiniHUD;
import fi.dy.masa.minihud.config.HighlighterConfigs;
import fi.dy.masa.minihud.config.PlayerHighlighterConfigs;
import fi.dy.masa.minihud.config.PlayerHighlighterConfigs.PlayerEntry;
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.config.RendererToggle;

/**
 * Renders outline-only boxes around players matching configured player names (multiplayer).
 * Uses same line width as block highlighter.
 */
public class OverlayRendererPlayerHighlighter extends OverlayRendererBase {
    public static final OverlayRendererPlayerHighlighter INSTANCE = new OverlayRendererPlayerHighlighter();

    private final List<PlayerHighlightEntry> entries = new ArrayList<>();
    private boolean hasData;

    private OverlayRendererPlayerHighlighter() {
        this.useCulling = false;
    }

    @Override
    public String getName() {
        return "PlayerHighlighter";
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return Configs.Generic.MAIN_RENDERING_TOGGLE.getBooleanValue() &&
                PlayerHighlighterConfigs.ENABLED.getBooleanValue() &&
                RendererToggle.OVERLAY_PLAYER_HIGHLIGHTER.getBooleanValue() &&
                hasAnyEnabledEntry();
    }

    private static boolean hasAnyEnabledEntry() {
        for (PlayerEntry e : PlayerHighlighterConfigs.getEntries())
            if (e.isEnabled() && e.getPlayerName() != null && !e.getPlayerName().isEmpty())
                return true;
        return false;
    }

    public void invalidate() {
        this.lastUpdatePos = null;
    }

    @Override
    public boolean needsUpdate(Entity entity, Minecraft mc) {
        return true;
    }

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
        if (!entries.isEmpty())
            this.renderObjects.add(new RenderObjectVbo(() -> this.getName() + "/Outlines",
                    MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL));
    }

    private static AABB getInterpolatedAabb(Entity entity, float partialTick) {
        Vec3 pos = entity.getPosition(partialTick);
        double w = entity.getBbWidth() * 0.5;
        double h = entity.getBbHeight();
        return new AABB(
                pos.x - w, pos.y, pos.z - w,
                pos.x + w, pos.y + h, pos.z + w);
    }

    private static boolean isInFrontOfPlayer(Vec3 cameraPos, Vec3 look, Entity entity) {
        double ex = entity.getX() - cameraPos.x;
        double ey = entity.getY() + entity.getBbHeight() * 0.5 - cameraPos.y;
        double ez = entity.getZ() - cameraPos.z;
        double lenSq = ex * ex + ey * ey + ez * ez;
        if (lenSq < 1e-6)
            return true;
        double invLen = 1.0 / Math.sqrt(lenSq);
        return (ex * invLen * look.x + ey * invLen * look.y + ez * invLen * look.z) > 0;
    }

    @Override
    public void update(Vec3 cameraPos, Entity entity, Minecraft mc, ProfilerFiller profiler) {
        entries.clear();
        hasData = false;
        if (mc.level == null || mc.player == null)
            return;

        int distance = Math.max(1, PlayerHighlighterConfigs.HIGHLIGHT_DISTANCE.getIntegerValue());
        long maxDistSq = (long) distance * distance;
        Vec3 cam = cameraPos;
        AABB box = new AABB(
                entity.getX() - distance, entity.getY() - distance, entity.getZ() - distance,
                entity.getX() + distance, entity.getY() + distance, entity.getZ() + distance);
        List<Entity> entities = mc.level.getEntitiesOfClass(Entity.class, box, e -> e instanceof Player);

        for (Entity e : entities) {
            if (e == mc.player)
                continue;
            Player player = (Player) e;
            double dx = e.getX() - cam.x;
            double dy = e.getY() + e.getBbHeight() * 0.5 - cam.y;
            double dz = e.getZ() - cam.z;
            if (dx * dx + dy * dy + dz * dz > maxDistSq)
                continue;

            String name = player.getName().getString();
            if (name == null)
                name = "";

            int argb = 0;
            for (PlayerEntry pe : PlayerHighlighterConfigs.getEntries()) {
                if (!pe.isEnabled())
                    continue;
                String entryName = pe.getPlayerName();
                if (entryName == null || entryName.isEmpty())
                    continue;
                if (entryName.equals(name) || entryName.equalsIgnoreCase(name)) {
                    argb = pe.getColorArgb();
                    break;
                }
            }
            if (argb != 0)
                entries.add(new PlayerHighlightEntry(player, argb));
        }

        hasData = !entries.isEmpty();
        this.renderThrough = true;
        this.glLineWidth = (float) HighlighterConfigs.LINE_WIDTH.getDoubleValue();

        if (hasData())
            render(cameraPos, mc, profiler);
    }

    @Override
    public void render(Vec3 cameraPos, Minecraft mc, ProfilerFiller profiler) {
        Vec3 look = mc.player != null ? mc.player.getLookAngle() : Vec3.ZERO;
        allocateBuffers(true);
        if (entries.isEmpty() || renderObjects.isEmpty())
            return;

        boolean anyInFront = false;
        for (PlayerHighlightEntry e : entries)
            if (isInFrontOfPlayer(cameraPos, look, e.entity)) {
                anyInFront = true;
                break;
            }
        if (!anyInFront)
            return;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        RenderObjectVbo ctx = renderObjects.get(0);
        BufferBuilder builder = ctx.start(() -> "minihud:player_highlighter/outlines",
                MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL);
        for (PlayerHighlightEntry e : entries) {
            if (!isInFrontOfPlayer(cameraPos, look, e.entity))
                continue;
            AABB aabb = getInterpolatedAabb(e.entity, partialTick);
            RenderUtils.drawBoxOutlinesAabb(aabb, cameraPos, argbToColor4fOutline(e.argb), this.glLineWidth, builder);
        }
        try {
            MeshData meshData = builder.build();
            if (meshData != null) {
                ctx.upload(meshData, false);
                meshData.close();
            }
        } catch (Exception err) {
            MiniHUD.LOGGER.error("PlayerHighlighter outlines: {}", err.getMessage());
        }
    }

    @Override
    public void reset() {
        super.reset();
        entries.clear();
        hasData = false;
    }

    private static final class PlayerHighlightEntry {
        final Player entity;
        final int argb;

        PlayerHighlightEntry(Player entity, int argb) {
            this.entity = entity;
            this.argb = argb;
        }
    }
}
