package com.timeofftracker.persistence;

import com.timeofftracker.model.EntryStatus;
import com.timeofftracker.model.TimeOffEntry;
import com.timeofftracker.model.TimeOffType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimeOffRepository {
    private final DatabaseManager db;

    public TimeOffRepository(DatabaseManager db) {
        this.db = db;
    }

    public List<TimeOffEntry> findByYear(int year) {
        return findBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    public List<TimeOffEntry> findBetween(LocalDate start, LocalDate end) {
        String sql = "SELECT id, entry_date, type, status, hours, notes FROM time_off_entries WHERE entry_date BETWEEN ? AND ? ORDER BY entry_date";
        List<TimeOffEntry> result = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load time off entries", e);
        }
    }

    public Optional<TimeOffEntry> findByDate(LocalDate date) {
        String sql = "SELECT id, entry_date, type, status, hours, notes FROM time_off_entries WHERE entry_date = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load time off entry", e);
        }
    }

    public TimeOffEntry save(TimeOffEntry entry) {
        if (entry.id() > 0) {
            update(entry);
            return entry;
        }
        String sql = "INSERT INTO time_off_entries(entry_date, type, status, hours, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entry);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : 0;
                return entry.withId(id);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save time off entry. A date may only contain one entry.", e);
        }
    }

    private void update(TimeOffEntry entry) {
        String sql = "UPDATE time_off_entries SET entry_date=?, type=?, status=?, hours=?, notes=? WHERE id=?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, entry);
            ps.setLong(6, entry.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update time off entry", e);
        }
    }

    private void bind(PreparedStatement ps, TimeOffEntry entry) throws SQLException {
        ps.setString(1, entry.date().toString());
        ps.setString(2, entry.type().name());
        ps.setString(3, entry.status().name());
        ps.setDouble(4, entry.hours());
        ps.setString(5, entry.notes() == null ? "" : entry.notes().trim());
    }

    public void delete(long id) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM time_off_entries WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete time off entry", e);
        }
    }

    private TimeOffEntry map(ResultSet rs) throws SQLException {
        return new TimeOffEntry(
                rs.getLong("id"),
                LocalDate.parse(rs.getString("entry_date")),
                TimeOffType.valueOf(rs.getString("type")),
                EntryStatus.valueOf(rs.getString("status")),
                rs.getDouble("hours"),
                rs.getString("notes")
        );
    }
}
