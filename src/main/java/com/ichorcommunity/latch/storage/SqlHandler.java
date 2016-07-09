package com.ichorcommunity.latch.storage;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.SQLException;

public class SqlHandler {

    //private final String jdbcUrl = "jdbc:h2:latch.db" + Latch.

    private enum TblStructure_Lock {
        ID              ("bigint AUTO_INCREMENT, "),
        OWNER_UUID      ("char(36) NOT NULL UNIQUE, "),
        LOCK_NAME       ("varchar(16), "),
        LOCK_TYPE       ("varchar(15), ");



        private final String attribute;
        TblStructure_Lock(String attribute) {this.attribute=attribute;}

        public String getAttribute(){return this.attribute;}

    }

    private SqlService sql;

    private DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return sql.getDataSource(jdbcUrl);
    }

    private void createTables() throws SQLException {
        Sponge.getServiceManager().provide(SqlService.class).get();
    }

}
