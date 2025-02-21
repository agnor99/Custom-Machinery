package fr.frinn.custommachinery.json_schema.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.frinn.custommachinery.json_schema.Init;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class AppearanceBuilder {
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  @Pattern(regexp = Init.RL)
  private String block;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  @Pattern(regexp = Init.RL)
  private String item;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  @Min(0)
  @Max(15)
  private int light;
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  private int color;
}
