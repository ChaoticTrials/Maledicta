package de.melanx.maledicta.storm;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.maledicta.ModConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

public class CurseStormCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("storm")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("start").executes(CurseStormCommand::start))
                .then(Commands.literal("stop").executes(CurseStormCommand::stop))
                .then(Commands.literal("status").executes(CurseStormCommand::status));
    }

    private static int start(CommandContext<CommandSourceStack> ctx) {
        CurseStorm storm = CurseStormCommand.storm(ctx);
        if (storm == null) {
            return 0;
        }

        storm.setPhase(CurseStormCommand.nether(ctx), StormPhase.ACTIVE);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.maledicta.storm.start", CurseStormCommand.seconds(storm.ticksLeft())), true);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        CurseStorm storm = CurseStormCommand.storm(ctx);
        if (storm == null) {
            return 0;
        }

        if (storm.phase() != StormPhase.BREWING && storm.phase() != StormPhase.ACTIVE) {
            ctx.getSource().sendFailure(Component.translatable("command.maledicta.storm.not_running"));
            return 0;
        }

        storm.setPhase(CurseStormCommand.nether(ctx), StormPhase.CLEARING);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.maledicta.storm.stop"), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        CurseStorm storm = CurseStormCommand.storm(ctx);
        if (storm == null) {
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.translatable("command.maledicta.storm.status",
                Component.translatable("command.maledicta.storm.phase." + storm.phase().getSerializedName()),
                CurseStormCommand.seconds(storm.ticksLeft())), false);
        return 1;
    }

    private static CurseStorm storm(CommandContext<CommandSourceStack> ctx) {
        if (!ModConfig.CurseStorm.enabled) {
            ctx.getSource().sendFailure(Component.translatable("command.maledicta.storm.disabled"));
            return null;
        }

        ServerLevel nether = CurseStormCommand.nether(ctx);
        CurseStorm storm = nether == null ? null : CurseStorm.get(nether);
        if (storm == null) {
            ctx.getSource().sendFailure(Component.translatable("command.maledicta.storm.no_nether"));
        }

        return storm;
    }

    private static ServerLevel nether(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getServer().getLevel(Level.NETHER);
    }

    private static int seconds(int ticks) {
        return Math.max(ticks, 0) / 20;
    }
}
