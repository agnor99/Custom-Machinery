package fr.frinn.custommachinery.json_schema.recipe.requirements;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.frinn.custommachinery.json_schema.Init;
import fr.frinn.custommachinery.json_schema.utils.RequirementIOMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class FluidRequirement extends Requirement {
  @JsonProperty(access = JsonProperty.Access.READ_WRITE, required = true)
  private RequirementIOMode mode;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE, required = true)
  @Pattern(regexp = "#?" + Init.RL)
  private String fluid;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE, required = true)
  private long amount;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  @Min(0)
  @Max(1)
  private double chance;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  private Object nbt;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  private String tank;
}
