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

            // Create mood_tracker table for storing mood assessments
            st.executeUpdate("""
                create table if not exists mood_tracker (
                  id          bigserial primary key,
                  soul_id     text not null,
                  mood_score  integer not null,
                  stress_score integer,
                  anxiety_score integer,
                  energy_score integer,
                  sleep_score integer,
                  social_score integer,
                  answers     text,
                  created_at  timestamptz default now()
                )
            """);

            st.executeUpdate("""
                create index if not exists idx_mood_soul on mood_tracker(soul_id)
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
            // Create keeper_signups table for admin registration requests
            st.executeUpdate("""
                create table if not exists keeper_signups (
                  keeper_id       text primary key,
                  email           text not null unique,
                  password_hash   text not null,
                  email_verified  boolean default false,
                  status          text default 'PENDING' check (status in ('PENDING', 'APPROVED', 'REJECTED')),
                  created_at      timestamptz default now(),
                  approved_at     timestamptz,
                  approved_by     text,
                  rejected_at     timestamptz,
                  rejected_by     text,
                  constraint keeper_id_is_lower check (keeper_id = lower(keeper_id))
                )
            """);
            
            // Create keepers table for approved admins
            st.executeUpdate("""
                create table if not exists keepers (
                  keeper_id       text primary key,
                  email           text not null unique,
                  password_hash   text not null,
                  short_name      text,
                  phone           text,
                  country_code    text,
                  blood_group     text,
                  approved_at     timestamptz default now(),
                  approved_by     text,
                  created_at      timestamptz default now(),
                  last_login      timestamptz,
                  constraint keeper_id_is_lower check (keeper_id = lower(keeper_id))
                )
            """);
            
            // Create index on email for fast lookups
            st.executeUpdate("""
                create index if not exists idx_keeper_signups_email on keeper_signups(email)
            """);
            st.executeUpdate("""
                create index if not exists idx_keepers_email on keepers(email)
            """);
            
            // Add profile columns to keepers table if they don't exist
            st.executeUpdate("""
                do $$
                begin
                  if not exists (select 1 from information_schema.columns where table_name='keepers' and column_name='short_name') then
                    alter table keepers add column short_name text;
                  end if;
                  if not exists (select 1 from information_schema.columns where table_name='keepers' and column_name='phone') then
                    alter table keepers add column phone text;
                  end if;
                  if not exists (select 1 from information_schema.columns where table_name='keepers' and column_name='country_code') then
                    alter table keepers add column country_code text;
                  end if;
                  if not exists (select 1 from information_schema.columns where table_name='keepers' and column_name='blood_group') then
                    alter table keepers add column blood_group text;
                  end if;
                end $$
            """);
            
            // Add rejection columns to keeper_signups table if they don't exist
            st.executeUpdate("""
                do $$
                begin
                  if not exists (select 1 from information_schema.columns where table_name='keeper_signups' and column_name='rejected_at') then
                    alter table keeper_signups add column rejected_at timestamptz;
                  end if;
                  if not exists (select 1 from information_schema.columns where table_name='keeper_signups' and column_name='rejected_by') then
                    alter table keeper_signups add column rejected_by text;
                  end if;
                end $$
            """);
            
            // Add last_activity column to soul_id_and_soul_key table for tracking active users
            st.executeUpdate("""
                do $$
                begin
                  if not exists (select 1 from information_schema.columns where table_name='soul_id_and_soul_key' and column_name='last_activity') then
                    alter table soul_id_and_soul_key add column last_activity timestamptz;
                  end if;
                end $$
            """);
            
            // Create index on last_activity for fast active user queries
            st.executeUpdate("""
                create index if not exists idx_soul_last_activity on soul_id_and_soul_key(last_activity)
            """);
            
            // Create password reset tokens table
            st.executeUpdate("""
                create table if not exists keeper_password_resets (
                  token         text primary key,
                  keeper_id     text not null references keepers(keeper_id) on delete cascade,
                  expires_at    timestamptz not null,
                  used          boolean default false,
                  created_at    timestamptz default now()
                )
            """);
            
            // Create index on keeper_id for password reset lookups
            st.executeUpdate("""
                create index if not exists idx_password_resets_keeper on keeper_password_resets(keeper_id)
            """);
            
            // Create index on expires_at and used for cleanup
            st.executeUpdate("""
                create index if not exists idx_password_resets_expiry on keeper_password_resets(expires_at, used)
            """);
            
            // Create moderation messages table for journal moderation
            st.executeUpdate("""
                create table if not exists moderation_messages (
                  message_id      serial primary key,
                  journal_id      int not null,
                  soul_id         text not null references soul_id_and_soul_key(soul_id) on delete cascade,
                  keeper_id       text not null references keepers(keeper_id) on delete cascade,
                  message_content text not null,
                  is_read         boolean default false,
                  created_at      timestamptz default now()
                )
            """);
            
            // Create index on soul_id for fetching user messages
            st.executeUpdate("""
                create index if not exists idx_moderation_messages_soul on moderation_messages(soul_id)
            """);
            
            // Create index on journal_id for checking moderation history
            st.executeUpdate("""
                create index if not exists idx_moderation_messages_journal on moderation_messages(journal_id)
            """);
            
            // Create index on keeper_id for admin tracking
            st.executeUpdate("""
                create index if not exists idx_moderation_messages_keeper on moderation_messages(keeper_id)
            """);
            
            // Create index on is_read for unread message counts
            st.executeUpdate("""
                create index if not exists idx_moderation_messages_read on moderation_messages(soul_id, is_read)
            """);
            
            // Blood support tables
            st.executeUpdate("""
                create table if not exists blood_requests (
                  id          bigserial primary key,
                  soul_id     text,
                  blood_group text not null,
                  location    text not null,
                  phone       text not null,
                  created_at  timestamptz default now()
                )
            """);

            st.executeUpdate("""
                create table if not exists blood_donors (
                  id                 bigserial primary key,
                  soul_id            text,
                  blood_group        text not null,
                  contact_number     text not null,
                  last_donation_info text,
                  area               text not null,
                  created_at         timestamptz default now()
                )
            """);

            st.executeUpdate("create index if not exists idx_blood_requests_soul on blood_requests(soul_id)");
            st.executeUpdate("create index if not exists idx_blood_requests_group on blood_requests(blood_group)");
            st.executeUpdate("create index if not exists idx_blood_donors_group on blood_donors(blood_group)");
            st.executeUpdate("create index if not exists idx_blood_donors_area on blood_donors(area)");

            // Consultation tables
            st.executeUpdate("""
                create table if not exists doctors (
                  id              bigserial primary key,
                  name            text not null,
                  degree          text not null,
                  phone           text not null,
                  consulting_hours text not null,
                  specialization  text,
                  created_at      timestamptz default now()
                )
            """);

            st.executeUpdate("""
                create table if not exists appointments (
                  id                bigserial primary key,
                  soul_id           text not null,
                  doctor_id         bigint not null references doctors(id),
                  appointment_date  text not null,
                  status            text default 'PENDING' check (status in ('PENDING', 'CONFIRMED', 'CANCELLED')),
                  created_at        timestamptz default now()
                )
            """);

            st.executeUpdate("create index if not exists idx_appointments_soul on appointments(soul_id)");
            st.executeUpdate("create index if not exists idx_appointments_doctor on appointments(doctor_id)");
            
            // Add safety plan columns to soul_id_and_soul_key table
            st.executeUpdate("""
                do $$
                begin
                  if not exists (
                    select 1 from information_schema.columns
                    where table_name = 'soul_id_and_soul_key' and column_name = 'safety_plan_contact'
                  ) then
                    alter table soul_id_and_soul_key 
                    add column safety_plan_contact text,
                    add column safety_plan_calm text,
                    add column safety_plan_place text;
                  end if;
                end $$
            """);
        }
    }
}
