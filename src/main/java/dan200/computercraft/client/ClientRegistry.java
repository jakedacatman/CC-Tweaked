/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.TurtleModelLoader;
import dan200.computercraft.shared.common.IColouredItem;
import dan200.computercraft.shared.media.items.ItemDisk;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;

/**
 * Registers textures and models for items.
 */
@Mod.EventBusSubscriber( modid = ComputerCraft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD )
public final class ClientRegistry
{
    private static final String[] EXTRA_MODELS = new String[] {
        "turtle_modem_normal_off_left",
        "turtle_modem_normal_on_left",
        "turtle_modem_normal_off_right",
        "turtle_modem_normal_on_right",

        "turtle_modem_advanced_off_left",
        "turtle_modem_advanced_on_left",
        "turtle_modem_advanced_off_right",
        "turtle_modem_advanced_on_right",
        "turtle_crafting_table_left",
        "turtle_crafting_table_right",

        "turtle_speaker_upgrade_left",
        "turtle_speaker_upgrade_right",

        "turtle_colour",
        "turtle_elf_overlay",
    };

    private static final String[] EXTRA_TEXTURES = new String[] {
        // TODO: Gather these automatically from the model. Sadly the model loader isn't available
        //  when stitching textures.
        "block/turtle_colour",
        "block/turtle_elf_overlay",
        "block/turtle_crafty_face",
        "block/turtle_speaker_face",
    };

    private ClientRegistry() {}

    @SubscribeEvent
    public static void registerModels( ModelRegistryEvent event )
    {
        ModelLoaderRegistry.registerLoader( new ResourceLocation( ComputerCraft.MOD_ID, "turtle" ), TurtleModelLoader.INSTANCE );
    }

    @SubscribeEvent
    public static void onTextureStitchEvent( TextureStitchEvent.Pre event )
    {
        // TODO: if( event.getMap() != Minecraft.getInstance().getTextureManager() ) return;

        for( String extra : EXTRA_TEXTURES )
        {
            event.addSprite( new ResourceLocation( ComputerCraft.MOD_ID, extra ) );
        }
    }

    @SubscribeEvent
    public static void onModelBakeEvent( ModelBakeEvent event )
    {
        // Load all extra models
        ModelLoader loader = event.getModelLoader();
        Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();

        for( String modelName : EXTRA_MODELS )
        {
            ResourceLocation location = new ResourceLocation( ComputerCraft.MOD_ID, "item/" + modelName );
            IUnbakedModel model = loader.getUnbakedModel( location );
            model.func_225614_a_( loader::getUnbakedModel, new HashSet<>() );
            IBakedModel baked = model.func_225613_a_( loader, ModelLoader.defaultTextureGetter(), SimpleModelTransform.IDENTITY, location );
            if( baked != null ) registry.put( new ModelResourceLocation( location, "inventory" ), baked );
        }
    }

    @SubscribeEvent
    public static void onItemColours( ColorHandlerEvent.Item event )
    {
        if( ComputerCraft.Items.disk == null || ComputerCraft.Blocks.turtleNormal == null )
        {
            ComputerCraft.log.warn( "Block/item registration has failed. Skipping registration of item colours." );
            return;
        }

        event.getItemColors().register(
            ( stack, layer ) -> layer == 1 ? ((ItemDisk) stack.getItem()).getColour( stack ) : 0xFFFFFF,
            ComputerCraft.Items.disk
        );

        event.getItemColors().register( ( stack, layer ) -> {
            switch( layer )
            {
                case 0:
                default:
                    return 0xFFFFFF;
                case 1: // Frame colour
                    return IColouredItem.getColourBasic( stack );
                case 2: // Light colour
                {
                    int light = ItemPocketComputer.getLightState( stack );
                    return light == -1 ? Colour.Black.getHex() : light;
                }
            }
        }, ComputerCraft.Items.pocketComputerNormal, ComputerCraft.Items.pocketComputerAdvanced );

        // Setup turtle colours
        event.getItemColors().register(
            ( stack, tintIndex ) -> tintIndex == 0 ? ((IColouredItem) stack.getItem()).getColour( stack ) : 0xFFFFFF,
            ComputerCraft.Blocks.turtleNormal, ComputerCraft.Blocks.turtleAdvanced
        );
    }
}
