package com.the_pathfinders.db;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ToDoRepository {

    public List<ToDoItem> loadForSoul(String soulId) throws SQLException {
        List<ToDoItem> out = new ArrayList<>();
        String sql = "select id, soul_id, task_text, done, created_at from todo_items where soul_id = ? order by created_at";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soulId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String sId = rs.getString("soul_id");
                    String task = rs.getString("task_text");
                    boolean done = rs.getBoolean("done");
                    OffsetDateTime created = rs.getObject("created_at", OffsetDateTime.class);
                    out.add(new ToDoItem(id, sId, task, done, created));
                }
            }
        }
        return out;
    }

    public ToDoItem insert(ToDoItem item) throws SQLException {
        String sql = "insert into todo_items(soul_id, task_text, done) values (?, ?, ?) returning id, created_at";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getSoulId());
            ps.setString(2, item.getTask());
            ps.setBoolean(3, item.isDone());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item.setId(rs.getLong("id"));
                    item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                }
            }
        }
        return item;
    }

    public void update(ToDoItem item) throws SQLException {
        String sql = "update todo_items set task_text = ?, done = ? where id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getTask());
            ps.setBoolean(2, item.isDone());
            ps.setLong(3, item.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "delete from todo_items where id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}