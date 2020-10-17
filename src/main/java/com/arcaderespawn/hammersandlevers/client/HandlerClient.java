package com.arcaderespawn.hammersandlevers.client;

import com.arcaderespawn.hammersandlevers.HammersAndLevers;
import com.arcaderespawn.hammersandlevers.items.FirearmBase;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.annotation.Target;


@Mod.EventBusSubscriber(modid = HammersAndLevers.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HandlerClient {
    private static final Logger LOGGER = LogManager.getLogger(HammersAndLevers.MODID + " Client Mod Event Subscriber");

    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {

    }

    private static void renderAmmuntionCount()
    {

    }

    @SubscribeEvent
    public static void onDrawGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            final ClientPlayerEntity player = Minecraft.getInstance().player;
            PlayerInventory inv = player.inventory;

            float scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
            float scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();



            for (Integer i = 0; i < 9; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof FirearmBase) {
                    float x =  (scaledWidth/2) - 90 + i * 20 + 2;
                    float y = scaledHeight - 16 - 3;

                    FontRenderer fr = stack.getItem().getFontRenderer(stack);
                    if(fr == null ) fr = Minecraft.getInstance().fontRenderer;

                    String count = ((FirearmBase)stack.getItem()).getAmmoString(stack);

                    MatrixStack matrixstack = new MatrixStack();
                    matrixstack.translate(0.0D, 0.0D, (double) (200.0F));
                    IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
                    fr.renderString(count, (float) (x + 19 - 2 - fr.getStringWidth(count)), (float) (y + 6 + 3), 0xFF0000, true, matrixstack.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
                    irendertypebuffer$impl.finish();
                }
            }
        }
    }
}
