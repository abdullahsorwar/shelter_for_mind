package com.the_pathfinders.db;

import java.sql.Connection;
import java.sql.Statement;

public final class DbMigrations {
    private DbMigrations() {}

    public static void runAll() throws Exception {
        try (Connection c = DB.getConnection(); Statement st = c.createStatement()) {
            // Create the table if it doesn't exist; keep soul_id lowercase for consistency
            st.executeUpdate("""
                create table if not exists soul_id_and_soul_key (
                  soul_id        text primary key,
                  soul_key_hash  text not null,
                  soul_name      text not null,
                  dob            date,
                  mobile         text,
                  country_code   text,
                  created_at     timestamptz default now(),
                  constraint soul_id_is_lower check (soul_id = lower(soul_id))
                )
            """);
        }
    }
}