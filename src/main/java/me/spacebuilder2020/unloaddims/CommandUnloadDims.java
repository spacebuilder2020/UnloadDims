package me.spacebuilder2020.unloaddims;

import static me.spacebuilder2020.unloaddims.CommonProxy.chatConfirm;
import static me.spacebuilder2020.unloaddims.CommonProxy.chatNotify;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;

import me.eigenraven.personalspace.world.PersonalWorldProvider;

public class CommandUnloadDims extends CommandBase implements ICommand {

    @Override
    public String getCommandName() {
        return "unloadpsdims";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Unloads all Personal Space Dims with no players in them";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        List<WorldServer> worlds = Arrays.stream(DimensionManager.getWorlds())
            .filter(worldServer -> worldServer.provider instanceof PersonalWorldProvider)
            .collect(Collectors.toList());
        if (worlds.isEmpty()) {
            chatNotify(sender, "No Personal Space dims found to unload!");
            return;
        }
        for (WorldServer world : worlds) {
            if (world.playerEntities.stream()
                .noneMatch(
                    entityPlayer -> entityPlayer instanceof EntityPlayerMP && !(entityPlayer instanceof FakePlayer))) {
                chatConfirm(sender, "Unloading Dim: " + world.provider.dimensionId);
                DimensionManager.unloadWorld(world.provider.dimensionId);
            } else {
                chatNotify(sender, "Dim: " + world.provider.dimensionId + " is not empty and will not be unloaded!");
            }
        }

    }

}
