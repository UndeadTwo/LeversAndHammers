package com.arcaderespawn.hammersandlevers;

import com.arcaderespawn.hammersandlevers.items.HenryRifle;
import com.arcaderespawn.hammersandlevers.items.LematPistol;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HammersAndLevers.MODID)
public class HammersAndLevers {
    public static final String MODID = "hammersandlevers";
    public static final String MODNAME = "Hammers and Levers";
    public static final String VERSION = "${version}";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static Item rifleCartridgeItem = new Item(new Item.Properties());
    public static Item riflePaperCartridgeItem = new Item(new Item.Properties());
    public static Item pistolCartridgeItem = new Item(new Item.Properties());
    public static Item pistolPaperCartridgeItem = new Item(new Item.Properties());
    public static Item shotgunShellItem = new Item(new Item.Properties());

    public HammersAndLevers() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        // InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        //        LOGGER.info("Got IMC {}", event.getIMCStream().
        //                map(m->m.getMessageSupplier().get()).
        //                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            rifleCartridgeItem.setRegistryName("rifle_cartridge");
            riflePaperCartridgeItem.setRegistryName("rifle_cartridge_paper");
            pistolCartridgeItem.setRegistryName("pistol_cartridge");
            pistolPaperCartridgeItem.setRegistryName("pistol_cartridge_paper");
            shotgunShellItem.setRegistryName("shell");

            // Register ammo items
            itemRegistryEvent.getRegistry().registerAll(rifleCartridgeItem, riflePaperCartridgeItem, pistolCartridgeItem, pistolPaperCartridgeItem, shotgunShellItem);

            // Register weapons
            itemRegistryEvent.getRegistry().registerAll(new HenryRifle(new Item.Properties()));
            itemRegistryEvent.getRegistry().registerAll(new LematPistol(new Item.Properties()));
        }
    }
}
