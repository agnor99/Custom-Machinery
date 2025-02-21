package fr.frinn.custommachinery.client.integration.jei.wrapper;

import dev.architectury.fluid.FluidStack;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Optional;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private final RequirementIOMode mode;
    private final IIngredient<Fluid> fluid;
    private final long amount;
    private final double chance;
    private final boolean isPerTick;
    private final CompoundTag nbt;
    private final String tank;

    public FluidIngredientWrapper(RequirementIOMode mode, IIngredient<Fluid> fluid, long amount, double chance, boolean isPerTick, CompoundTag nbt, String tank) {
        this.mode = mode;
        this.fluid = fluid;
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.nbt = nbt;
        this.tank = tank;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof FluidGuiElement fluidElement) || element.getType() != Registration.FLUID_GUI_ELEMENT.get())
            return false;

        List<FluidStack> ingredients = this.fluid.getAll().stream().map(fluid -> FluidStack.create(fluid, this.amount, this.nbt)).toList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(fluidElement);
        if(fluidElement.getID().equals(this.tank) || template.map(t -> t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.tank.isEmpty() || t.getId().equals(this.tank))).orElse(false)) {
            IRecipeSlotBuilder slot = builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset + 1, element.getY() - yOffset + 1)
                    .setFluidRenderer(this.amount, false, element.getWidth() - 2, element.getHeight() - 2)
                    .addTooltipCallback((view, tooltips) -> {
                        if(this.isPerTick)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.fluid.pertick"));

                        if(this.chance == 0)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
                        else if(this.chance != 1.0)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(this.chance * 100)));

                        if(!this.tank.isEmpty() && Minecraft.getInstance().options.advancedItemTooltips)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.fluid.specificTank").withStyle(ChatFormatting.DARK_RED));
                    });
            this.addFluidIngredients(helper.getJeiHelpers().getPlatformFluidHelper(), slot);
            return true;
        }
        return false;
    }

    public <T> void addFluidIngredients(IPlatformFluidHelper<T> helper, IRecipeSlotBuilder builder) {
        IIngredientType<T> fluidType = helper.getFluidIngredientType();
        List<T> stacks = this.fluid.getAll().stream().map(fluid -> helper.create(fluid, this.amount, this.nbt)).toList();
        builder.addIngredients(fluidType, stacks);
    }
}
