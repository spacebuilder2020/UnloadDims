package me.spacebuilder2020.unloaddims;

import static me.spacebuilder2020.unloaddims.CommonProxy.chatNotify;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;

public class CommandUnloadChunks extends CommandBase implements ICommand {

    @Override
    public String getCommandName() {
        return "unloadchunks";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Unloads all chunks in current dim without a player nearby (10 chunks / view-distance)";
    }

    public boolean playersNearChunk(List<EntityPlayer> players, Chunk chunk, int dist) {
        for (EntityPlayer player : players) {
            int x = Math.abs(player.chunkCoordX - chunk.xPosition);
            int z = Math.abs(player.chunkCoordZ - chunk.zPosition);
            if (x <= dist || z <= dist) {
                return true;
            }
        }
        return false;
    }

    public boolean isForcedChunk(World world, Chunk chunk) {
        return world.getPersistentChunks()
            .containsKey(new ChunkCoordIntPair(chunk.xPosition, chunk.zPosition));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        final int dim;
        if (args.length == 0) {
            dim = sender.getEntityWorld().provider.dimensionId;
        } else {
            dim = Integer.parseInt(args[0]);
        }

        final int dist;

        if (args.length > 1) {
            dist = Integer.parseInt(args[1]);
        } else if (MinecraftServer.getServer()
            .isDedicatedServer()) {
                dist = ((DedicatedServer) MinecraftServer.getServer()).getIntProperty("view-distance", 10);
            } else {
                dist = 10;
            }

        final World world = DimensionManager.getWorld(dim);

        List<EntityPlayer> playerlist = world.playerEntities.stream()
            .filter(entityPlayer -> entityPlayer instanceof EntityPlayerMP && !(entityPlayer instanceof FakePlayer))
            .collect(Collectors.toList());

        IChunkProvider chunkProvider = world.getChunkProvider();
        chatNotify(
            sender,
            String.format(
                "Loaded: %d Forced: %d",
                chunkProvider.getLoadedChunkCount(),
                world.getPersistentChunks()
                    .size()));

        if (chunkProvider instanceof ChunkProviderServer) {
            List<Chunk> chunksToUnload = ((ChunkProviderServer) chunkProvider).loadedChunks.stream()
                .filter(chunk -> !playersNearChunk(playerlist, chunk, dist) && !isForcedChunk(world, chunk))
                .collect(Collectors.toList());
            chatNotify(sender, String.format("Orphan Chunks: %d", chunksToUnload.size()));

            chunksToUnload.forEach(
                chunk -> ((ChunkProviderServer) chunkProvider).chunksToUnload
                    .add(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition)));
            int unloadedChunks;
            do {
                int queuedChunks = ((ChunkProviderServer) chunkProvider).chunksToUnload.size();
                chunkProvider.unloadQueuedChunks();
                unloadedChunks = queuedChunks - ((ChunkProviderServer) chunkProvider).chunksToUnload.size();

                chatNotify(sender, String.format("Queued: %d Unloaded: %d", queuedChunks, unloadedChunks));
            } while (unloadedChunks == 100 || !((ChunkProviderServer) chunkProvider).chunksToUnload.isEmpty());

        }
    }
}
