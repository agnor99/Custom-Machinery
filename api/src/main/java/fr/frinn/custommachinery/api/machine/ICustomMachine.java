package fr.frinn.custommachinery.api.machine;

import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface ICustomMachine {

    /**
     * @return The name of this machine, as specified in the "name" property of the machine json.
     */
    Component getName();

    /**
     * @return The id of the machine. Path of the id will be the path of the machine json file in the datapack.
     */
    ResourceLocation getId();

    /**
     * @return The recipe ids supported by this machine.
     * Normal machines just return their id but upgraded machines return both their id and their parent machine id.
     */
    List<ResourceLocation> getRecipeIds();

    /**
     * @return true if the machine is DUMMY, usually indicate that something went wrong, or a machine was not found.
     */
    boolean isDummy();

    /**
     * @return The MachineAppearance corresponding to the supplied MachineStatus.
     */
    IMachineAppearance getAppearance(MachineStatus status);

    /**
     * @return A template for the processor used to process recipes.
     */
    IProcessorTemplate<?> getProcessorTemplate();
}
