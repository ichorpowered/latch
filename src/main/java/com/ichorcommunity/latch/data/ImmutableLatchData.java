package com.ichorcommunity.latch.data;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableLatchData extends AbstractImmutableData<ImmutableLatchData, LatchData> {

    private boolean isLocked;

    public ImmutableLatchData() {
        this(false);
    }

    public ImmutableLatchData(boolean locked) {
        this.isLocked = locked;
    }

    public ImmutableValue<Boolean> isLocked() {
        return Sponge.getRegistry().getValueFactory().createValue(LatchKeys.IS_LOCKED, this.isLocked, false).asImmutable();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(LatchKeys.IS_LOCKED, this::isLocked);
        registerKeyValue(LatchKeys.IS_LOCKED, this::isLocked);
    }

    @Override
    public LatchData asMutable() {
        return new LatchData(this.isLocked);
    }

    @Override
    public int compareTo(ImmutableLatchData o) {
        return ComparisonChain.start()
                .compare(o.isLocked, this.isLocked)
                .result();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(LatchKeys.IS_LOCKED, this.isLocked);
    }
}
