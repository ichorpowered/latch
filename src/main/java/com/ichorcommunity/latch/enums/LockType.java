package com.ichorcommunity.latch.enums;

public enum LockType {
    /*
     * Accessible by everyone
     */
    PUBLIC,

    /*
     * Requires a password every time to use
     */
    PASSWORD_ALWAYS,

    /*
     * Requires a password the first time a player accesses it
     */
    PASSWORD_ONCE,

    /*
     * Owned/controlled by one person, access able to be shared
     */
    PRIVATE,

    /*
     * Allows all players to deposit items, only the owner to withdraw
     */
    DONATION;

}
