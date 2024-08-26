package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;


import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ObjectBlock extends Block
{


    public ObjectBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @org.jetbrains.annotations.Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if(Screen.hasShiftDown())
        {
            pTooltip.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            pTooltip.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }
}
