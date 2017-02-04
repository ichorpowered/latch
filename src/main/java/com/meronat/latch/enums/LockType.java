/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.meronat.latch.enums;

public enum LockType {

    /*
     * Accessible by everyone
     */
    PUBLIC("Public"),

    /*
     * Requires a password every time to use
     */
    PASSWORD_ALWAYS("Password Always"),

    /*
     * Requires a password the first time a player accesses it
     */
    PASSWORD_ONCE("Password Once"),

    /*
     * Owned/controlled by one person, access able to be shared
     */
    PRIVATE("Private"),

    /*
     * Allows all players to deposit items, only the owner to withdraw
     */
    DONATION("Donation");

    private final String humanReadable;

    LockType(String human) {
        this.humanReadable = human;
    }

    @Override
    public String toString() {
        return this.humanReadable;
    }

}
