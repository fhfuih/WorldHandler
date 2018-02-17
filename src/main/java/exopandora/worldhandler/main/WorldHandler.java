package exopandora.worldhandler.main;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import exopandora.worldhandler.builder.ICommandBuilder;
import exopandora.worldhandler.builder.ICommandBuilderSyntax;
import exopandora.worldhandler.command.CommandWH;
import exopandora.worldhandler.command.CommandWorldHandler;
import exopandora.worldhandler.config.ConfigButcher;
import exopandora.worldhandler.config.ConfigSettings;
import exopandora.worldhandler.config.ConfigSkin;
import exopandora.worldhandler.config.ConfigSliders;
import exopandora.worldhandler.gui.category.Category;
import exopandora.worldhandler.gui.content.Content;
import exopandora.worldhandler.helper.BlockHelper;
import exopandora.worldhandler.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid = Main.MODID, name = Main.NAME, acceptedMinecraftVersions = "[" + Main.MC_COMPATIBLE + ",)", version = Main.VERSION, canBeDeactivated = false, guiFactory = "exopandora.worldhandler.gui.config.GuiFactoryWorldHandler", updateJSON = Main.UPDATE_URL, clientSideOnly = true)
public class WorldHandler
{
	@Instance(Main.MODID)
	public WorldHandler INSTANCE;
	
	public static KeyBinding KEY_WORLD_HANDLER = new KeyBinding(Main.NAME, Keyboard.KEY_V, "key.categories.misc");
	public static KeyBinding KEY_WORLD_HANDLER_POS1 = new KeyBinding(Main.NAME + " Pos1", Keyboard.KEY_O, "key.categories.misc");
	public static KeyBinding KEY_WORLD_HANDLER_POS2 = new KeyBinding(Main.NAME + " Pos2", Keyboard.KEY_P, "key.categories.misc");
	
	public static Logger LOGGER;
	
	public static final ICommand COMMAND_WORLD_HANDLER = new CommandWorldHandler();
	public static final ICommand COMMAND_WH = new CommandWH();
	
	public static Configuration CONFIG;
	
	public static String USERNAME = null;
	
	@SidedProxy(clientSide = "exopandora.worldhandler.proxy.ClientProxy", serverSide = "exopandora.worldhandler.proxy.CommonProxy")
	public static CommonProxy PROXY;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		LOGGER = event.getModLog();
		LOGGER.info("Pre-Initializing " + Main.NAME_AND_VERSION);
		LOGGER.info("First Release on March 28 2013 - 02:29 PM CET by Exopandora");
		LOGGER.info("Latest Version: " + Main.URL);
		CONFIG = new Configuration(event.getSuggestedConfigurationFile());
		
		ConfigSettings.load(CONFIG);
		ConfigSkin.load(CONFIG);
		ConfigSliders.load(CONFIG);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		LOGGER.info("Initializing " + Main.NAME_AND_VERSION);
		USERNAME = Minecraft.getMinecraft().getSession().getUsername();
		
		MinecraftForge.EVENT_BUS.register(new exopandora.worldhandler.event.EventHandler());
		ClientRegistry.registerKeyBinding(KEY_WORLD_HANDLER);
		updateKeyBindings();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		LOGGER.info("Post-Initializing " + Main.NAME_AND_VERSION);
		LOGGER.info("Every mod that has not been registered to this point may not be fully featured in this mod");
		
		ConfigButcher.load(CONFIG);
		Content.registerContents();
		Category.registerCategories();
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(COMMAND_WORLD_HANDLER);
		event.registerServerCommand(COMMAND_WH);
	}
	
	public static void updateConfig()
	{
		ConfigSettings.load(CONFIG);
		ConfigSkin.load(CONFIG);
		ConfigButcher.load(CONFIG);
		ConfigSliders.load(CONFIG);
		updateKeyBindings();
	}
	
	public static void updateKeyBindings()
	{
		boolean isRegistered = ArrayUtils.contains(Minecraft.getMinecraft().gameSettings.keyBindings, KEY_WORLD_HANDLER_POS1) || ArrayUtils.contains(Minecraft.getMinecraft().gameSettings.keyBindings, KEY_WORLD_HANDLER_POS2);
		
		if(ConfigSettings.arePosShortcutsEnabled() && !isRegistered)
		{
			ClientRegistry.registerKeyBinding(KEY_WORLD_HANDLER_POS1);
			ClientRegistry.registerKeyBinding(KEY_WORLD_HANDLER_POS2);
		}
		else if(!ConfigSettings.arePosShortcutsEnabled() && isRegistered)
		{
			Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.removeElements(Minecraft.getMinecraft().gameSettings.keyBindings, KEY_WORLD_HANDLER_POS1, KEY_WORLD_HANDLER_POS2);
		}
	}
	
	public static void sendCommand(ICommandBuilder builder)
	{
		sendCommand(builder, false);
	}
	
	public static void sendCommand(ICommandBuilder builder, boolean special)
	{
		if(builder != null)
		{
			String command;
			
			if(builder instanceof ICommandBuilderSyntax)
			{
				command = ((ICommandBuilderSyntax) builder).toActualCommand();
			}
			else
			{
				command = builder.toCommand();
			}
			
			LOGGER.info("Command: " + command);
			
			if(builder.needsCommandBlock() || special)
			{
				BlockHelper.setCommandBlockNearPlayer(command);
			}
			else
			{
				Minecraft.getMinecraft().player.sendChatMessage(command);
			}
		}
	}
}