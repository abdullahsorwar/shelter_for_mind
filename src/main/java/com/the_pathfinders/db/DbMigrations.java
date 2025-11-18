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


            
            // Create soul_info table to store user profile information
            st.executeUpdate("""
                create table if not exists soul_info (
                  soul_id     text primary key,
                  name        text,
                  dob         date,
                  email       text,
                  phone       text,
                  address     text,
                  country_code text,
                  created_at  timestamptz default now(),
                  updated_at  timestamptz default now()
                )
            """);
            // Create todo_items table for storing user tasks
            st.executeUpdate("""
                create table if not exists todo_items (
                  id          bigserial primary key,
                  soul_id     text not null,
                  task_text   text not null,
                  done        boolean default false,
                  created_at  timestamptz default now()
                )
            """);

            st.executeUpdate("""
                create index if not exists idx_todo_soul on todo_items(soul_id)
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
            
            // Add missing columns for soul_info if needed (for existing installations)
            st.executeUpdate("""
                do $$
                begin
                  if not exists (
                    select 1 from information_schema.tables where table_name = 'soul_info'
                  ) then
                    create table soul_info (
                      soul_id     text primary key,
                      name        text,
                      dob         date,
                      email       text,
                      phone       text,
                      address     text,
                      country_code text,
                      created_at  timestamptz default now(),
                      updated_at  timestamptz default now()
                    );
                  else
                    if not exists (select 1 from information_schema.columns where table_name='soul_info' and column_name='email') then
                      alter table soul_info add column email text;
                    end if;
                    if not exists (select 1 from information_schema.columns where table_name='soul_info' and column_name='phone') then
                      alter table soul_info add column phone text;
                    end if;
                    if not exists (select 1 from information_schema.columns where table_name='soul_info' and column_name='address') then
                      alter table soul_info add column address text;
                    end if;
                    if not exists (select 1 from information_schema.columns where table_name='soul_info' and column_name='country_code') then
                      alter table soul_info add column country_code text;
                    end if;
                    if not exists (select 1 from information_schema.columns where table_name='soul_info' and column_name='created_at') then
                      alter table soul_info add column created_at timestamptz default now();
                    end if;
                    if not exists (select 1 from information_schema.columns where table_name='soul_info' and column_name='updated_at') then
                      alter table soul_info add column updated_at timestamptz default now();
                    end if;
                  end if;
                end $$
            """);
            
            // Create GIN index on loved_by array for fast searches (binary search-like performance)
            st.executeUpdate("""
                create index if not exists idx_loved_by_gin on public_journals using gin (loved_by)
            """);
            
            // Add isPublic column for visibility control
            st.executeUpdate("""
                do $$
                begin
                  if not exists (
                    select 1 from information_schema.columns
                    where table_name = 'public_journals' and column_name = 'is_public'
                  ) then
                    alter table public_journals add column is_public boolean default true;
                  end if;
                end $$
            """);
            
            // Add email_verified column to track email verification status
            st.executeUpdate("""
                do $$
                begin
                  if not exists (
                    select 1 from information_schema.columns
                    where table_name = 'soul_info' and column_name = 'email_verified'
                  ) then
                    alter table soul_info add column email_verified boolean default false;
                  end if;
                end $$
            """);
        }
    }
}