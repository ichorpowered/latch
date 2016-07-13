package com.ichorcommunity.latch.data;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class LatchDataManipulatorBuilder implements DataManipulatorBuilder<LatchData, ImmutableLatchData> {
    /**
     * Creates a new specific {@link DataManipulator} for consumption.
     *
     * @return The newly created data manipulator
     */
    @Override
    public LatchData create() {
        return new LatchData();
    }

    /**
     * Attempts to read data from the given {@link DataHolder} and constructs
     * a new copy of the {@link DataManipulator} as an instance of
     * <code>T</code>.
     * <p>
     * <p>If the {@link DataHolder} does not contain the necessary information
     * to pre-populate the {@link DataManipulator}, a fresh new
     * {@link DataManipulator} is returned. If the {@link DataManipulator} is
     * incompatible with the {@link DataHolder}, {@link Optional#empty()} is
     * returned.</p>
     *
     * @param dataHolder The {@link DataHolder} to extract data
     * @return A new instance of this {@link DataManipulator} with relevant data
     * filled from the given {@link DataHolder}, if available
     */
    @Override
    public Optional<LatchData> createFrom(DataHolder dataHolder) {
        return Optional.of(dataHolder.get(LatchData.class).orElse(create()));
    }

    /**
     * Attempts to build the provided {@link DataSerializable} from the given
     * {@link DataView}. If the {@link DataView} is invalid or
     * missing necessary information to complete building the
     * {@link DataSerializable}, {@link Optional#empty()} may be returned.
     *
     * @param container The container containing all necessary data
     * @return The instance of the {@link DataSerializable}, if successful
     * @throws InvalidDataException In the event that the builder is unable to
     *                              properly construct the data serializable from the data view
     */
    @Override
    public Optional<LatchData> build(DataView container) throws InvalidDataException {
        // Note that this should check the Queries.CONTENT_VERSION, but for the sake of demonstration
        // it's not necessary
        if (container.contains(LatchKeys.IS_LOCKED)) {
            final boolean isLocked = container.getBoolean(LatchKeys.IS_LOCKED.getQuery()).get();
            return Optional.of(new LatchData(isLocked));
        }
        return Optional.empty();
    }
}
