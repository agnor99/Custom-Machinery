package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.common.network.sync.data.FluidStackData;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class FluidStackSyncable extends AbstractSyncable<FluidStackData, FluidStack> {

    @Override
    public FluidStackData getData(short id) {
        return new FluidStackData(id, get());
    }

    @Override
    public boolean needSync() {
        FluidStack value = get();
        boolean needSync;
        if(this.lastKnownValue != null)
            needSync = !value.isFluidStackIdentical(this.lastKnownValue);
        else needSync = true;
        this.lastKnownValue = value.copy();
        return needSync;
    }

    public static FluidStackSyncable create(Supplier<FluidStack> supplier, Consumer<FluidStack> consumer) {
        return new FluidStackSyncable() {
            @Override
            public FluidStack get() {
                return supplier.get();
            }

            @Override
            public void set(FluidStack value) {
                consumer.accept(value);
            }
        };
    }
}
