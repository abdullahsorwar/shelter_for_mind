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
            
            // Create public journals table for journal entries
            st.executeUpdate("""
                create table if not exists public_journals (
                  journal_id   char(7) primary key not null,
                  soul_id      text,
                  journal_text text not null,
                  love_count   integer default 0,
                  loved_by     text[] default '{}',
                  font_family  text default 'System',
                  font_size    integer default 14,
                  created_at   timestamptz default now()
                )
            """);
            
            // Add loved_by column if it doesn't exist (for existing tables)
            st.executeUpdate("""
                do $$
                begin
                  if not exists (
                    select 1 from information_schema.columns
                    where table_name = 'public_journals' and column_name = 'loved_by'
                  ) then
                    alter table public_journals add column loved_by text[] default '{}';
                  end if;
                end $$
            """);
            
            // Add font columns if they don't exist (for existing tables)
            st.executeUpdate("""
                do $$
                begin
                  if not exists (
                    select 1 from information_schema.columns
                    where table_name = 'public_journals' and column_name = 'font_family'
                  ) then
                    alter table public_journals add column font_family text default 'System';
                  end if;
                  if not exists (
                    select 1 from information_schema.columns
                    where table_name = 'public_journals' and column_name = 'font_size'
                  ) then
                    alter table public_journals add column font_size integer default 14;
                  end if;
                end $$
            """);
        }
    }
}