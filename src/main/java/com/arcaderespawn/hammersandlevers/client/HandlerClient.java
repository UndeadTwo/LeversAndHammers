package com.arcaderespawn.hammersandlevers.client;

import com.arcaderespawn.hammersandlevers.HammersAndLevers;
import com.arcaderespawn.hammersandlevers.items.FirearmBase;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
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

//    if (stack.getCount() != 1 || text != null) {
//        String s = text == null ? String.valueOf(stack.getCount()) : text;
//        matrixstack.translate(0.0D, 0.0D, (double)(this.zLevel + 200.0F));
//        IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
//        fr.renderString(s, (float)(xPosition + 19 - 2 - fr.getStringWidth(s)), (float)(yPosition + 6 + 3), 16777215, true, matrixstack.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
//        irendertypebuffer$impl.finish();
//    }

    @SubscribeEvent
    public static void onDrawGui(GuiScreenEvent event) {
        if (event instanceof GuiScreenEvent.DrawScreenEvent.Post) {
            LOGGER.debug("Post ContainerScreen draw");
            LOGGER.debug(event.getGui());
            if(event.getGui() instanceof  ContainerScreen) {
                ContainerScreen screenEvent = (ContainerScreen) event.getGui();

                for (Slot s : screenEvent.getContainer().inventorySlots) {
                    LOGGER.debug(s);
                    if (!s.getStack().isEmpty() && s.getStack().getItem() instanceof FirearmBase) {
                        LOGGER.debug("Draw gun event");
                        float x = s.xPos;
                        float y = s.yPos;

                        FontRenderer fr = s.getStack().getItem().getFontRenderer(s.getStack());
                        String count = String.valueOf(FirearmBase.getAmmoCount(s.getStack()));

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
}
