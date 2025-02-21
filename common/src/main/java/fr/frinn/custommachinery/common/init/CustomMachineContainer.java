package fr.frinn.custommachinery.common.init;

import dev.architectury.registry.fuel.FuelRegistry;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.FilterItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.ResultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.crafting.craft.CraftProcessor;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.slot.FilterSlotItemComponent;
import fr.frinn.custommachinery.common.util.slot.ResultSlotItemComponent;
import fr.frinn.custommachinery.common.util.slot.SlotItemComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMachineContainer extends SyncableContainer {

    private final Inventory playerInv;
    private final int firstComponentSlotIndex;
    private final CustomMachineTile tile;
    private boolean hasPlayerInventory = false;
    private final List<SlotItemComponent> inputSlotComponents = new ArrayList<>();

    public CustomMachineContainer(int id, Inventory playerInv, CustomMachineTile tile) {
        super(Registration.CUSTOM_MACHINE_CONTAINER.get(), id, tile, playerInv.player);
        this.playerInv = playerInv;
        this.tile = tile;

        CustomMachine machine = tile.getMachine();

        AtomicInteger slotIndex = new AtomicInteger(0);

        machine.getGuiElements()
            .stream()
            .filter(element -> element.getType() == Registration.PLAYER_INVENTORY_GUI_ELEMENT.get())
            .findFirst()
            .ifPresent(element -> {
                this.hasPlayerInventory = true;

                int x = element.getX() + 1;
                int y = element.getY() + 1;

                for(int k = 0; k < 9; ++k)
                    this.addSyncedSlot(new Slot(playerInv, slotIndex.getAndIncrement(), x + k * 18, y + 58));

                for(int i= 0; i < 3; ++i) {
                    for(int j = 0; j < 9; ++j)
                        this.addSyncedSlot(new Slot(playerInv, slotIndex.getAndIncrement(), x + j * 18, y + i * 18));
                }
            }
        );

        this.firstComponentSlotIndex = slotIndex.get();

        machine.getGuiElements()
            .stream()
            .filter(element -> element.getType() == Registration.SLOT_GUI_ELEMENT.get())
            .map(element -> (SlotGuiElement)element)
            .forEach(element -> {
                this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).flatMap(itemHandler -> itemHandler.getComponentForID(element.getID())).ifPresent(component -> {
                    int x = element.getX();
                    int y = element.getY();
                    int width = element.getWidth();
                    int height = element.getHeight();
                    int slotX = x + (width - 16) / 2;
                    int slotY = y + (height - 16) / 2;
                    SlotItemComponent slotComponent;
                    if(component.getVariant() == ResultItemComponentVariant.INSTANCE)
                        slotComponent = new ResultSlotItemComponent(component, slotIndex.getAndIncrement(), slotX, slotY);
                    else if(component.getVariant() == FilterItemComponentVariant.INSTANCE)
                        slotComponent = new FilterSlotItemComponent(component, slotIndex.getAndIncrement(), slotX, slotY);
                    else
                        slotComponent = new SlotItemComponent(component, slotIndex.getAndIncrement(), slotX, slotY);
                    this.addSlot(slotComponent);
                    if(component.getVariant() != DefaultItemComponentVariant.INSTANCE || component.getMode().isInput())
                        this.inputSlotComponents.add(slotComponent);
                });
            }
        );
    }

    public CustomMachineContainer(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, ClientHandler.getClientSideCustomMachineTile(extraData.readBlockPos()));
    }

    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId) instanceof SlotItemComponent slot && !slot.getItem().isEmpty()) {
            tile.getComponentManager().getComponent(Registration.EXPERIENCE_MACHINE_COMPONENT.get()).ifPresent(
                component -> {
                    if (component.canRetrieveFromSlots()) {
                        if (component.slotsFromCanRetrieve().isEmpty()) {
                            player.giveExperiencePoints(Utils.toInt(component.getXp()));
                            component.extractXp(component.getXp(), false);
                        } else {
                            component.slotsFromCanRetrieve().forEach(id -> {
                                if (id.equals(slot.getComponent().getId())) {
                                    player.giveExperiencePoints(Utils.toInt(component.getXp()));
                                    component.extractXp(component.getXp(), false);
                                }
                            });
                        }
                    }
                }
            );
        }
        this.tile.setChanged();
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if(!this.hasPlayerInventory)
            return ItemStack.EMPTY;

        Slot clickedSlot = this.slots.get(index);
        if(clickedSlot.getItem().isEmpty())
            return ItemStack.EMPTY;

        if(clickedSlot.container == this.playerInv) {
            ItemStack stack = clickedSlot.getItem().copy();
            List<SlotItemComponent> components;
            if(!CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(stack.getItem(), this.tile.getId()).isEmpty())
                components = this.inputSlotComponents.stream().sorted(Comparator.comparingInt(slot -> slot.getComponent().getVariant() == UpgradeItemComponentVariant.INSTANCE ? -1 : 1)).toList();
            else if(FuelRegistry.get(stack) > 0)
                components = this.inputSlotComponents.stream().sorted(Comparator.comparingInt(slot -> slot.getComponent().getVariant() == FuelItemComponentVariant.INSTANCE ? -1 : 1)).toList();
            else
                components = this.inputSlotComponents;
            for (SlotItemComponent slotComponent : components) {
                if(slotComponent.getComponent().isLocked())
                    continue;
                int maxInput = slotComponent.getComponent().insert(stack.getItem(), stack.getCount(), stack.getTag(), true, true);
                if(maxInput > 0) {
                    int toInsert = Math.min(maxInput, stack.getCount());
                    slotComponent.getComponent().insert(stack.getItem(), toInsert, stack.getTag(), false, true);
                    stack.shrink(toInsert);
                }
                if(stack.isEmpty())
                    break;
            }
            if(stack.isEmpty())
                clickedSlot.remove(clickedSlot.getItem().getCount());
            else
                clickedSlot.remove(clickedSlot.getItem().getCount() - stack.getCount());
        } else {
            if (!(clickedSlot instanceof SlotItemComponent slotComponent) || slotComponent.getComponent().isLocked())
                return ItemStack.EMPTY;

            if(slotComponent instanceof ResultSlotItemComponent resultSlot && this.tile.getProcessor() instanceof CraftProcessor processor) {
                ItemStack removed = slotComponent.getItem().copy();
                if(!this.playerInv.add(removed))
                    return ItemStack.EMPTY;
                slotComponent.setChanged();

                int crafted = 0;
                while (processor.bulkCraft()) {
                    removed = slotComponent.getItem().copy();
                    if(!this.playerInv.add(removed))
                        return ItemStack.EMPTY;
                    slotComponent.setChanged();
                    if(crafted++ == 64 && player.getAbilities().instabuild) return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }

            ItemStack removed = slotComponent.getItem();
            if(!moveItemStackTo(removed, 0, this.firstComponentSlotIndex - 1, false))
                return ItemStack.EMPTY;
            slotComponent.setChanged();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level.getBlockState(this.tile.getBlockPos()) == this.tile.getBlockState() &&
                player.level.getBlockEntity(this.tile.getBlockPos()) == this.tile &&
                player.position().distanceToSqr(Vec3.atCenterOf(this.tile.getBlockPos())) <= 64;
    }

    @Override
    public boolean needFullSync() {
        return this.tile.getLevel() != null && this.tile.getLevel().getGameTime() % 100 == 0;
    }

    public void elementClicked(int element, byte button) {
        if(element < 0 || element >= this.tile.getMachine().getGuiElements().size())
            throw new IllegalArgumentException("Invalid gui element ID: " + element);
        this.tile.getMachine().getGuiElements().get(element).handleClick(button, this.tile, this, this.getPlayer());
    }
}
