package com.ichorcommunity.latch.data;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

public class LatchKeys {

    public static final Key<Value<Boolean>> IS_LOCKED = makeSingleKey(Boolean.class, Value.class, of("IsLocked"));

}
