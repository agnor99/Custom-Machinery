package fr.frinn.custommachinery.common.crafting.machine;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.crafting.AbstractRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

public class CustomMachineRecipeBuilder extends AbstractRecipeBuilder<CustomMachineRecipe> {

    public static final NamedCodec<CustomMachineRecipeBuilder> CODEC = NamedCodec.record(recipeBuilderInstance ->
            recipeBuilderInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("machine").forGetter(AbstractRecipeBuilder::getMachine),
                    NamedCodec.INT.fieldOf("time").forGetter(builder -> builder.time),
                    IRequirement.CODEC.listOf().optionalFieldOf("requirements", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getRequirements),
                    IRequirement.CODEC.listOf().optionalFieldOf("jei", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getJeiRequirements),
                    NamedCodec.INT.optionalFieldOf("priority", 0).forGetter(AbstractRecipeBuilder::getPriority),
                    NamedCodec.INT.optionalFieldOf("jeiPriority", 0).forGetter(AbstractRecipeBuilder::getJeiPriority),
                    NamedCodec.BOOL.optionalFieldOf("error", true).forGetter(builder -> !builder.resetOnError),
                    NamedCodec.BOOL.optionalFieldOf("hidden", false).forGetter(AbstractRecipeBuilder::isHidden),
                    MachineAppearance.CODEC.optionalFieldOf("appearance").forGetter(builder -> Optional.ofNullable(builder.appearance).map(MachineAppearance::getProperties))
            ).apply(recipeBuilderInstance, (machine, time, requirements, jeiRequirements, priority, jeiPriority, error, hidden, appearance) -> {
                    CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(machine, time);
                    requirements.forEach(builder::withRequirement);
                    jeiRequirements.forEach(builder::withJeiRequirement);
                    builder.withPriority(priority);
                    builder.withJeiPriority(jeiPriority);
                    if (!error)
                        builder.setResetOnError();
                    if(hidden)
                        builder.hide();
                    appearance.ifPresent(map -> builder.withAppearance(new MachineAppearance(map)));
                    return builder;
            }), "Machine recipe builder"
    );

    private final int time;
    private boolean resetOnError = false;
    @Nullable
    private MachineAppearance appearance = null;

    public CustomMachineRecipeBuilder(ResourceLocation machine, int time) {
        super(machine);
        this.time = time;
    }

    public CustomMachineRecipeBuilder(CustomMachineRecipe recipe) {
        super(recipe);
        this.time = recipe.getRecipeTime();
        this.resetOnError = recipe.shouldResetOnError();
        this.appearance = recipe.getAppearance();
    }

    public CustomMachineRecipeBuilder setResetOnError() {
        this.resetOnError = true;
        return this;
    }

    public CustomMachineRecipeBuilder withAppearance(MachineAppearance appearance) {
        this.appearance = appearance;
        return this;
    }

    public CustomMachineRecipe build(ResourceLocation id) {
        return new CustomMachineRecipe(id, this.getMachine(), this.time, this.getRequirements(), this.getJeiRequirements(), this.getPriority(), this.getJeiPriority(), this.resetOnError, this.isHidden(), this.appearance);
    }

    @Override
    public String toString() {
        return "CustomMachineRecipe{" +
                "machine=" + getMachine() +
                ", time=" + time +
                ", requirements=" + getRequirements().stream().map(requirement -> requirement.getType().getId()).toList() +
                ", jeiRequirements=" + getJeiRequirements().stream().map(requirement -> requirement.getType().getId()).toList() +
                ", priority=" + getPriority() +
                ", jeiPriority=" + getJeiPriority() +
                ", resetOnError=" + resetOnError +
                ", hidden=" + isHidden() +
                '}';
    }
}
