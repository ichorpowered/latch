package com.ichorcommunity.latch.entities;

import com.ichorcommunity.latch.enums.LockType;

//TODO potentially convert this to a handler if more data/different interactions are possible (i.e. changing lock data)

public class LockCreationData {

    private LockType type;
    private String password;

    public LockCreationData(LockType type, String password) {
        this.type = type;
        this.password = password;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

}
