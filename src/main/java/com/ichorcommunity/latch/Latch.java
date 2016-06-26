package com.ichorcommunity.latch;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "latch",
        name = "Latch",
        version = "0.0.1",
        description = "A locking plugin which optionally allows you to lockpick those locks.",
        url = "http://ichorcommunity.com/",
        authors = {
                "Nighteyes604",
                "Meronat"
        }
)
public class Latch {

    @Inject
    private Logger logger;

}