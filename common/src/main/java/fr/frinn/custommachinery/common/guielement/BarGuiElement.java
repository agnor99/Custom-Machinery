package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class BarGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_EMPTY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage_empty.png");
    public static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage_filled.png");

    public static final NamedCodec<BarGuiElement> CODEC = NamedCodec.record(barGuiElementInstance ->
            barGuiElementInstance.group(
                    makePropertiesCodec().forGetter(BarGuiElement::getProperties),
                    NamedCodec.STRING.fieldOf("id").forGetter(BarGuiElement::getId),
                    NamedCodec.INT.optionalFieldOf("min", 0).forGetter(BarGuiElement::getMin),
                    NamedCodec.INT.optionalFieldOf("max", 1000).forGetter(BarGuiElement::getMax),
                    NamedCodec.BOOL.optionalFieldOf("highlight", true).forGetter(BarGuiElement::isHighlight),
                    NamedCodec.enumCodec(Orientation.class).optionalFieldOf("orientation", Orientation.TOP).forGetter(BarGuiElement::getOrientation),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_empty", BASE_EMPTY_TEXTURE).forGetter(BarGuiElement::getEmptyTexture),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_filled", BASE_FILLED_TEXTURE).forGetter(BarGuiElement::getFilledTexture)
            ).apply(barGuiElementInstance, BarGuiElement::new), "Bar gui element"
    );

    private final String id;
    private final int min;
    private final int max;
    private final boolean highlight;
    private final Orientation orientation;
    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;

    public BarGuiElement(Properties properties, String id, int min, int max, boolean highlight, Orientation orientation, ResourceLocation emptyTexture, ResourceLocation filledTexture) {
        super(properties, emptyTexture);
        this.id = id;
        this.min = min;
        this.max = max;
        this.highlight = highlight;
        this.orientation = orientation;
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
    }

    @Override
    public GuiElementType<BarGuiElement> getType() {
        return Registration.BAR_GUI_ELEMENT.get();
    }

    public String getId() {
        return this.id;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean isHighlight() {
        return this.highlight;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
        return this.filledTexture;
    }
}
