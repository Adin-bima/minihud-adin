package fi.dy.masa.minihud.info.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.Reference;
import fi.dy.masa.minihud.config.InfoToggle;
import fi.dy.masa.minihud.info.InfoLine;
import fi.dy.masa.minihud.info.InfoLineContext;

/**
 * HUD info line: last death coordinates and dimension (Overworld / Nether /
 * End).
 */
public class InfoLineLastDeathCoordinate extends InfoLine {

    private static final String KEY = Reference.MOD_ID + ".info_line.last_death_coordinate";
    private static final String KEY_DIMENSION = Reference.MOD_ID + ".info_line.last_death_dimension";

    public InfoLineLastDeathCoordinate(InfoToggle type) {
        super(type);
    }

    public InfoLineLastDeathCoordinate() {
        super(InfoToggle.LAST_DEATH_COORDINATE);
    }

    @Override
    public boolean succeededType() {
        return false;
    }

    @Override
    public List<Entry> parse(@NotNull InfoLineContext ctx) {
        if (ctx.world() == null)
            return null;
        return ctx.ent() != null ? parseEnt(ctx.world(), ctx.ent()) : null;
    }

    @Override
    public List<Entry> parseEnt(@NotNull Level world, @NotNull Entity ent) {
        if (!(ent instanceof Player player))
            return null;

        // In singleplayer, client often doesn't have lastDeathLocation synced; use server player.
        Optional<GlobalPos> lastDeath = getLastDeathLocation(player);
        if (lastDeath.isEmpty()) {
            return List.of(this.translate(KEY + ".none"));
        }

        GlobalPos pos = lastDeath.get();
        int x = pos.pos().getX();
        int y = pos.pos().getY();
        int z = pos.pos().getZ();
        String dimName = getDimensionDisplayName(pos.dimension().identifier().toString());

        List<Entry> list = new ArrayList<>();
        list.add(this.translate(KEY, x, y, z, dimName));
        return list;
    }

    private Optional<GlobalPos> getLastDeathLocation(Player clientPlayer) {
        IntegratedServer server = getData().getIntegratedServer();
        if (server != null) {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(clientPlayer.getUUID());
            if (serverPlayer != null) {
                Optional<GlobalPos> opt = serverPlayer.getLastDeathLocation();
                if (opt.isPresent())
                    return opt;
            }
        }
        return clientPlayer.getLastDeathLocation();
    }

    private static String getDimensionDisplayName(String dimensionId) {
        return switch (dimensionId) {
            case "minecraft:overworld" -> StringUtils.translate(KEY_DIMENSION + ".overworld");
            case "minecraft:the_nether" -> StringUtils.translate(KEY_DIMENSION + ".nether");
            case "minecraft:the_end" -> StringUtils.translate(KEY_DIMENSION + ".end");
            default -> dimensionId;
        };
    }
}
