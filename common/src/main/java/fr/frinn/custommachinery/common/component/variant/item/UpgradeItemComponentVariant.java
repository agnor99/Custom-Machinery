package fr.frinn.custommachinery.common.component.variant.item;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class UpgradeItemComponentVariant extends ItemComponentVariant {

    public static final UpgradeItemComponentVariant INSTANCE = new UpgradeItemComponentVariant();
    public static final Codec<UpgradeItemComponentVariant> CODEC = Codec.unit(INSTANCE);
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "upgrade");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Codec<UpgradeItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return !CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(stack.getItem(), manager.getTile().getMachine().getId()).isEmpty();
    }
}
