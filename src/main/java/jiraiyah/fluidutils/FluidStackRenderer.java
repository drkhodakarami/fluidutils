package jiraiyah.fluidutils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>CREDIT: https://github.com/mezz/JustEnoughItems by mezz (Forge Version)
 * <p>HIGHLY EDITED VERSION FOR FABRIC by Kaupenjoe
 * <p>Under MIT-License: https://github.com/mezz/JustEnoughItems/blob/1.18/LICENSE.txt
 */
public class FluidStackRenderer implements IIngredientRenderer<FluidStack>
{
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    public final long capacityMb;
    private final TooltipMode tooltipMode;
    private final int width;
    private final int height;
    private int zOrder;

    enum TooltipMode
    {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }

    public FluidStackRenderer()
    {
        this(FluidUtils.convertDropletsToMb(FluidConstants.BUCKET), TooltipMode.SHOW_AMOUNT_AND_CAPACITY, 16, 16);
    }

    public FluidStackRenderer(long capacityMb, boolean showCapacity, int width, int height)
    {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public FluidStackRenderer(int capacityMb, boolean showCapacity, int width, int height)
    {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height);
    }

    private FluidStackRenderer(long capacityMb, TooltipMode tooltipMode, int width, int height)
    {
        Preconditions.checkArgument(capacityMb > 0, "capacity must be > 0");
        Preconditions.checkArgument(width > 0, "width must be > 0");
        Preconditions.checkArgument(height > 0, "height must be > 0");
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.zOrder = 1;
    }

    public void setzOrder(int index)
    {
        this.zOrder = index;
    }

    /**
     * <p>Renders the fluid amount in the screen background draw method.
     * <p>METHOD FROM https://github.com/TechReborn/TechReborn
     * <p>UNDER MIT LICENSE: https://github.com/TechReborn/TechReborn/blob/1.19/LICENSE.md
     *
     * @param context the draw context sent from the screen drawing method
     * @param fluid the fluid stack of the container
     * @param x the x coordinate to start drawing
     * @param y the y coordinate to start drawing
     * @param width the width of the drawing
     * @param height the height of the drawing at full capacity
     * @param maxCapacity the maximum capacity of the fluid stack
     */
    public void drawFluid(DrawContext context, FluidStack fluid, int x, int y, int width, int height, long maxCapacity)
    {
        if (fluid.getFluidVariant().getFluid() == Fluids.EMPTY)
            return;
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        y += height;
        final Sprite sprite = FluidVariantRendering.getSprite(fluid.getFluidVariant());
        int color = FluidVariantRendering.getColor(fluid.getFluidVariant());

        final int drawHeight = (int) (fluid.getAmount() / (maxCapacity * 1F) * height);
        final int iconHeight = sprite.getY();
        int offsetHeight = drawHeight;

        RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F, (float) (color >> 8 & 255) / 255.0F, (float) (color & 255) / 255.0F, 1F);

        int iteration = 0;
        while (offsetHeight != 0)
        {
            final int curHeight = offsetHeight < iconHeight ? offsetHeight : iconHeight;

            context.getMatrices().push();
            context.getMatrices().translate(0f, 0f, 0.01f * zOrder);
            context.drawSprite(x, y - offsetHeight, 0, width, curHeight, sprite);
            context.getMatrices().pop();
            offsetHeight -= curHeight;
            iteration++;
            if (iteration > 50)
                break;
        }
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.setShaderTexture(0, FluidRenderHandlerRegistry.INSTANCE.get(fluid.getFluidVariant().getFluid())
                .getFluidSprites(MinecraftClient.getInstance().world, null, fluid.getFluidVariant().getFluid().getDefaultState())[0].getAtlasId());
    }

    /**
     * <p>The method that will return the tooltip for fluid stack. you need to register few values in the lang file :
     * <p>modid + ".tooltip.liquid.amount.with.capacity" >>> example value : "%s / %s mB"
     * <p>modid + ".tooltip.liquid.amount" >>> example value : "%s mB"
     * <p>"block.minecraft.empty" >>> example value : "Empty"
     * @param fluidStack  The ingredient to get the tooltip for.
     * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
     * @param modid The mod id for the translation key in lang file
     * @return a list of Text objects as tooltips
     */
    @Override
    public List<Text> getTooltip(FluidStack fluidStack, TooltipContext tooltipFlag, String modid)
    {
        List<Text> tooltip = new ArrayList<>();
        FluidVariant fluidType = fluidStack.getFluidVariant();
        if (fluidType == null)
            return tooltip;

        MutableText displayName = Text.translatable("block." + Registries.FLUID.getId(fluidStack.fluidVariant.getFluid()).toTranslationKey());
        tooltip.add(displayName);

        long amount = fluidStack.getAmount();
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY)
        {
            MutableText amountString = Text.translatable(modid + ".tooltip.liquid.amount.with.capacity", nf.format(FluidUtils.convertDropletsToMb(amount)), nf.format(FluidUtils.convertDropletsToMb(capacityMb)));
            tooltip.add(amountString.fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }
        else if (tooltipMode == TooltipMode.SHOW_AMOUNT)
        {
            MutableText amountString = Text.translatable(modid + ".tooltip.liquid.amount", nf.format(FluidUtils.convertDropletsToMb(amount)));
            tooltip.add(amountString.fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }

        return tooltip;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    /**
     *  Checks if the mouse is above the given area or not
     * @param mouseX The mouse x position
     * @param mouseY The mouse y position
     * @param x Left boundary coordinate
     * @param y Top boundary coordinate
     * @param offsetX Offset from left boundary to get to the right boundary (boundary width)
     * @param offsetY Offset from top boundary to get to the bottom boundary (boundary height)
     * @return a boolean value indicating if the mouse is inside the given boundary or not
     */
    public boolean isMouseAboveArea(int mouseX, int mouseY, int x, int y, int offsetX, int offsetY)
    {
        return mouseX >= x + offsetX &&
                mouseX <= x + offsetX + getWidth() &&
                mouseY >= y + offsetY &&
                mouseY <= y + offsetY + getHeight();
    }
}