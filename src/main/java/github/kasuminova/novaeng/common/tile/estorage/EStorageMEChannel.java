package github.kasuminova.novaeng.common.tile.estorage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EStorageMEChannel extends EStoragePart implements ICellProvider, IActionHost, IGridProxyable, IAEPowerStorage {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "channel", getVisualItemStack(), true);
    protected final IActionSource source = new MachineSource(this);

    protected EStorageController storageController = null;

    protected int priority = 0;

    public EStorageMEChannel() {
        this.proxy.setIdlePowerUsage(1.0D);
        this.proxy.setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
    }

    public IActionSource getSource() {
        return source;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel<?> channel) {
        if (storageController != null) {
            return storageController.getCellDrives().stream()
                    .map(drive -> drive.getHandler(channel))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public ItemStack getVisualItemStack() {
        // TODO Change this.
        return ItemStack.EMPTY;
    }

    @Override
    public double injectAEPower(final double amt, @Nonnull final Actionable mode) {
        if (storageController == null) {
            return 0;
        }
        if (amt < 0.000001) {
            return 0;
        }
        if (mode == Actionable.MODULATE && this.getAECurrentPower() < 0.01 && amt > 0) {
            this.proxy.getNode().getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.PROVIDE_POWER));
        }
        return storageController.injectPower(amt, mode);
    }

    @Override
    public double extractAEPower(final double amt, @Nonnull final Actionable mode, @Nonnull final PowerMultiplier multiplier) {
        if (storageController == null) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            final boolean wasFull = this.getAECurrentPower() >= this.getAEMaxPower() - 0.001;
            if (wasFull && amt > 0) {
                try {
                    this.proxy.getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
                } catch (final GridAccessException ignored) {
                }
            }
        }
        return multiplier.divide(storageController.extractPower(multiplier.multiply(amt), mode));
    }

    @Override
    public double getAEMaxPower() {
        if (this.storageController == null) {
            return 0;
        }
        return this.storageController.getMaxEnergyStore();
    }

    @Override
    public double getAECurrentPower() {
        if (this.storageController == null) {
            return 0;
        }
        return this.storageController.getEnergyStored();
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Nonnull
    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return proxy.getNode();
    }

    @Nonnull
    @Override
    public AENetworkProxy getProxy() {
        return proxy;
    }

    @Nonnull
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {

    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation dir) {
        return proxy.getNode();
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        proxy.readFromNBT(compound);
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        proxy.writeToNBT(compound);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        proxy.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        proxy.invalidate();
    }

    @Override
    public void onAssembled() {
        super.onAssembled();
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(proxy::onReady);
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        proxy.invalidate();
    }

}
