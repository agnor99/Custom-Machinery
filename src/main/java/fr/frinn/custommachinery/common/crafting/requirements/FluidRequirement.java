package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Ingredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class FluidRequirement extends AbstractRequirement<FluidComponentHandler> implements IChanceableRequirement<FluidComponentHandler>, IJEIIngredientRequirement {

    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(fluidRequirementInstance ->
            fluidRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Ingredient.FluidIngredient.CODEC.fieldOf("fluid").forGetter(requirement -> requirement.fluid),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance),
                    Codec.STRING.optionalFieldOf("tank", "").forGetter(requirement -> requirement.tank)
            ).apply(fluidRequirementInstance, (mode, fluid, amount, chance, tank) -> {
                    FluidRequirement requirement = new FluidRequirement(mode, fluid, amount, tank);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private Ingredient.FluidIngredient fluid;
    private int amount;
    private double chance = 1.0D;
    private String tank;

    public FluidRequirement(MODE mode, Ingredient.FluidIngredient fluid, int amount, String tank) {
        super(mode);
        if(mode == MODE.OUTPUT && fluid.getObject() == null)
            throw new IllegalArgumentException("You must specify a fluid for an Output Fluid Requirement");
        this.fluid = fluid;
        this.amount = amount;
        this.tank = tank;
        this.fluidIngredientWrapper = new FluidIngredientWrapper(this.getMode(), this.fluid, this.amount, this.chance, false, this.tank);
    }

    @Override
    public RequirementType<FluidRequirement> getType() {
        return Registration.FLUID_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            return this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid)).sum() >= amount;
        }
        else {
            if(this.fluid.getObject() != null)
                return component.getSpaceForFluid(this.tank, this.fluid.getObject()) >= amount;
            else throw new IllegalStateException("Can't use output fluid requirement with fluid tag");
        }
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            int maxDrain = this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid)).sum();
            if(maxDrain >= amount) {
                int toDrain = amount;
                for (Fluid fluid : this.fluid.getAll()) {
                    int canDrain = component.getFluidAmount(this.tank, fluid);
                    if(canDrain > 0) {
                        canDrain = Math.min(canDrain, toDrain);
                        component.removeFromInputs(this.tank, new FluidStack(fluid, canDrain));
                        toDrain -= canDrain;
                        if(toDrain == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", this.fluid, amount, maxDrain));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.OUTPUT) {
            if(this.fluid.getObject() != null) {
                Fluid fluid = this.fluid.getObject();
                FluidStack stack = new FluidStack(fluid, amount);
                int canFill =  component.getSpaceForFluid(this.tank, fluid);
                if(canFill >= amount) {
                    component.addToOutputs(this.tank, stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.output", amount, new TranslationTextComponent(fluid.getAttributes().getTranslationKey())));
            } else throw new IllegalStateException("Can't use output fluid requirement with fluid tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean testChance(FluidComponentHandler component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    private FluidIngredientWrapper fluidIngredientWrapper;
    @Override
    public FluidIngredientWrapper getJEIIngredientWrapper() {
        return this.fluidIngredientWrapper;
    }
}
