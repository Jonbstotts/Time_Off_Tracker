package com.timeofftracker.persistence;

import com.timeofftracker.model.TimeOffYear;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YearRepository {
    private final DatabaseManager db;

    public YearRepository(DatabaseManager db) {
        this.db = db;
    }

    public Optional<TimeOffYear> findByYear(int year) {
        String sql = "SELECT year, vacation_allowance, eto_allowance, standard_workday FROM time_off_years WHERE year = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load year settings", e);
        }
    }

    public List<Integer> findAllYears() {
        List<Integer> years = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT year FROM time_off_years ORDER BY year");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) years.add(rs.getInt("year"));
            return years;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load configured years", e);
        }
    }

    public void save(TimeOffYear year) {
        String sql = """
                INSERT INTO time_off_years(year, vacation_allowance, eto_allowance, standard_workday)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(year) DO UPDATE SET
                    vacation_allowance = excluded.vacation_allowance,
                    eto_allowance = excluded.eto_allowance,
                    standard_workday = excluded.standard_workday
                """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year.year());
            ps.setDouble(2, year.vacationAllowanceHours());
            ps.setDouble(3, year.etoAllowanceHours());
            ps.setDouble(4, year.standardWorkdayHours());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save year settings", e);
        }
    }

    private TimeOffYear map(ResultSet rs) throws SQLException {
        return new TimeOffYear(
                rs.getInt("year"),
                rs.getDouble("vacation_allowance"),
                rs.getDouble("eto_allowance"),
                rs.getDouble("standard_workday")
        );
    }
}
