package com.ichorcommunity.latch.sql;

public class SqlHandler {

    private enum TblStructure_Lock {
        ID              ("bigint AUTO_INCREMENT, "),
        OWNER_UUID      ("char(36) NOT NULL UNIQUE, "),
        LOCK_NAME       ("varchar(16), "),
        LOCK_TYPE       ("varchar(15), ");



        private final String attribute;
        TblStructure_Lock(String attribute) {this.attribute=attribute;}

        public String getAttribute(){return this.attribute;}

    }

}
