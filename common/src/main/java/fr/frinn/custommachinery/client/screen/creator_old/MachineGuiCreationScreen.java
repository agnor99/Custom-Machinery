package fr.frinn.custommachinery.client.screen.creator_old;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.network.chat.Component;

public class MachineGuiCreationScreen extends MachineCreationTabScreen {

    public MachineGuiCreationScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(Component.literal("Machine Gui Creation"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.font.draw(matrix, "GUI", this.xPos + 20, this.yPos + 20, 0);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }
}
