package me.spacebuilder2020.unloaddims;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        CommandHandler handler = (CommandHandler) MinecraftServer.getServer()
            .getCommandManager();
        handler.registerCommand(new CommandUnloadDims());
        handler.registerCommand(new CommandUnloadChunks());
    }

    public static void chatConfirm(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.GREEN);
    }

    public static void chatError(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.RED);
    }

    public static void chatWarning(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.YELLOW);
    }

    public static void chatNotify(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.AQUA);
    }

    public static void sendMessage(ICommandSender sender, String message, EnumChatFormatting color) {
        ChatComponentText text = new ChatComponentText(message);
        text.getChatStyle()
            .setColor(color);
        sender.addChatMessage(text);
    }
}
