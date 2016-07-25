package com.ichorcommunity.latch.data;

import com.google.common.base.Objects;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

import static com.ichorcommunity.latch.Latch.getLogger;

public class LatchData extends AbstractData<LatchData, ImmutableLatchData> {

    private boolean isLocked;

    public LatchData() {
        this(false);
    }

    public LatchData(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Value<Boolean> isLocked() {
        return Sponge.getRegistry().getValueFactory().createValue(LatchKeys.IS_LOCKED, this.isLocked, false);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(LatchKeys.IS_LOCKED, () -> isLocked);
        registerFieldSetter(LatchKeys.IS_LOCKED, value -> this.isLocked = value);
        registerKeyValue(LatchKeys.IS_LOCKED, this::isLocked);
    }

    /**
     * Attempts to read data from the given {@link DataHolder} and fills the
     * associated data onto this {@link DataManipulator}. Any data that
     * overlaps between this and the given {@link DataHolder} will be resolved
     * using the given {@link MergeFunction}.
     * <p>
     * <p>Any data that overlaps existing data from the {@link DataHolder} will
     * take priority and be overwritten from the pre-existing data from the
     * {@link DataHolder}. It is recommended that a call from
     * {@link DataHolder#supports(Class)} is checked prior to using this
     * method on any {@link DataHolder}.</p>
     *
     * @param dataHolder The {@link DataHolder} to extract data
     * @param overlap    The overlap resolver to decide which data to retain
     * @return This {@link DataManipulator} with relevant data filled from the
     * given {@link DataHolder}, if compatible
     */
    @Override
    public Optional<LatchData> fill(DataHolder dataHolder, MergeFunction overlap) {
        return Optional.empty();
    }

    /**
     * Attempts to read the raw data from the provided {@link DataContainer}.
     * This manipulator should be "reset" to a default state and apply all data
     * from the given {@link DataContainer}. If data is missing from the
     * {@link DataContainer}, {@link Optional#empty()} can be returned.
     *
     * @param container The container of raw data
     * @return This {@link DataManipulator} with relevant data filled from the
     * given {@link DataContainer}, if compatible
     */
    @Override
    public Optional<LatchData> from(DataContainer container) {
        if(!container.contains(LatchKeys.IS_LOCKED.getQuery())) {
            return Optional.empty();
        }
        final boolean locked = container.getBoolean(LatchKeys.IS_LOCKED.getQuery()).get();
        this.isLocked = locked;
        return Optional.of(this);
    }

    @Override
    public LatchData copy() {
        return new LatchData(this.isLocked);
    }

    @Override
    public ImmutableLatchData asImmutable() {
        return new ImmutableLatchData(this.isLocked);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(LatchData o) {
        return 0;
    }

    /**
     * Gets the content version of this {@link DataSerializable}. The version
     * may differ between instances of plugins and implementations such that
     * the {@link DataView} from {@link #toContainer()} may include different
     * information, or remove other information as they are no longer deemend
     * necessary. The version goes hand in hand with {@link DataContentUpdater}
     * as it is required when there exists any {@link DataView} of this
     * {@link DataSerializable} with an "older" version.
     *
     * @return The version of the content being serialized
     */
    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(LatchKeys.IS_LOCKED, this.isLocked);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("locked", this.isLocked)
                .toString();
    }

}
