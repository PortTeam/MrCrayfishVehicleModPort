package com.mrcrayfish.vehicle.client.render.model;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Calendar;

/**
 * Author: MrCrayfish
 */
public class ChestModel
{
//    private final ModelBlockRenderer top;
//    private final ModelBlockRenderer base;
//    private final ModelBlockRenderer latch;
    private final boolean christmas;

    public ChestModel()
    {
        Calendar c = Calendar.getInstance();
        this.christmas = c.get(Calendar.MONTH) + 1 == 12 && c.get(Calendar.DAY_OF_MONTH) >= 24 && c.get(Calendar.DAY_OF_MONTH) <= 26;
        BlockColors colors = new BlockColors();
//        this.base = new ModelBlockRenderer(64, 64, 0, 19);
//       // this.base.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
//        this.top = new ModelBlockRenderer(64, 64, 0, 0);
//       // this.top.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
//        this.top.y = 9.0F;
//        this.top.z = 1.0f;
//        this.latch = new ModelBlockRenderer(64, 64, 0, 0);
//        this.latch.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
//        this.latch.y = 8.0F;
    }

    public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Pair<Float, Float> lidProgressPair, int light, float partialTicks)
    {
//        float lidProgress = Mth.lerp(partialTicks, lidProgressPair.getLeft(), lidProgressPair.getRight());
//        lidProgress = 1.0F - lidProgress;
//        lidProgress = 1.0F - lidProgress * lidProgress * lidProgress;
//        RenderMaterial renderMaterial = this.christmas ? TextureAtlas.CHEST_XMAS_LOCATION : Atlases.CHEST_LOCATION;
//        IVertexBuilder builder = renderMaterial.buffer(renderTypeBuffer, RenderType::entityCutout);
//        this.renderChest(matrixStack, builder, lidProgress, light);
    }

    private void renderChest(PoseStack matrixStack, MultiBufferSource builder, float openProgress, int lightTexture)
    {
//  //      this.top.xRot = -(openProgress * ((float) Math.PI / 2F));
//        this.latch.xRot = this.top.xRot;
//        this.top.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
//        this.latch.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
//        this.base.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
    }
}
