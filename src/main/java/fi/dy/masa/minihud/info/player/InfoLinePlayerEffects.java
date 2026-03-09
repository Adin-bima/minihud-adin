package fi.dy.masa.minihud.info.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import fi.dy.masa.minihud.config.InfoToggle;
import fi.dy.masa.minihud.info.InfoLine;
import fi.dy.masa.minihud.info.InfoLineContext;
import fi.dy.masa.minihud.Reference;
import fi.dy.masa.minihud.util.MiscUtils;

/**
 * HUD info line: current player's active status effects with remaining duration.
 */
public class InfoLinePlayerEffects extends InfoLine {

    private static final String EFFECTS_KEY = Reference.MOD_ID + ".info_line.player_effects";

    public InfoLinePlayerEffects(InfoToggle type) {
        super(type);
    }

    public InfoLinePlayerEffects() {
        this(InfoToggle.PLAYER_EFFECTS);
    }

    @Override
    public boolean succeededType() {
        return false;
    }

    @Override
    public List<Entry> parse(@Nonnull InfoLineContext ctx) {
        if (ctx.world() == null)
            return null;
        Entity ent = ctx.ent();
        if (ent instanceof Player player)
            return parseEnt(ctx.world(), player);
        return null;
    }

    @Override
    public List<Entry> parseEnt(@Nonnull Level world, @Nonnull Entity ent) {
        if (!(ent instanceof LivingEntity living))
            return null;

        Collection<MobEffectInstance> effects = living.getActiveEffects();
        List<Entry> list = new ArrayList<>();

        for (MobEffectInstance effect : effects) {
            if (effect.isInfiniteDuration() || effect.getDuration() > 0) {
                list.add(this.translate(EFFECTS_KEY,
                        effect.getEffect().value().getDisplayName().getString(),
                        effect.getAmplifier() > 0 ? this.qt(EFFECTS_KEY + ".amplifier", effect.getAmplifier() + 1) : "",
                        effect.isInfiniteDuration() ? this.qt(EFFECTS_KEY + ".infinite") :
                                MiscUtils.formatDuration((effect.getDuration() / 20) * 1000L),
                        this.qt(REMAINING_KEY)
                ));
            }
        }

        return list;
    }
}
