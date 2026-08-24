package edu.eci.arsw.blueprints.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementacion de BlueprintPersistence sobre PostgreSQL usando JdbcTemplate.
 * Se activa con el perfil de Spring "postgres" (ver application-postgres.properties).
 * Cada blueprint se guarda en la tabla "blueprints" y sus puntos, ordenados por
 * secuencia, en "blueprint_points" (ver schema.sql).
 */
@Repository
@Profile("postgres")
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbc;

    public PostgresBlueprintPersistence(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private boolean exists(String author, String name) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?",
                Integer.class, author, name);
        return count != null && count > 0;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (exists(bp.getAuthor(), bp.getName())) {
            throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        jdbc.update("INSERT INTO blueprints(author, name) VALUES (?, ?)", bp.getAuthor(), bp.getName());
        List<Point> points = bp.getPoints();
        for (int seq = 0; seq < points.size(); seq++) {
            Point p = points.get(seq);
            jdbc.update("INSERT INTO blueprint_points(author, name, seq, x, y) VALUES (?,?,?,?,?)",
                    bp.getAuthor(), bp.getName(), seq, p.x(), p.y());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        if (!exists(author, name)) {
            throw new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name));
        }
        List<Point> points = jdbc.query(
                "SELECT x, y FROM blueprint_points WHERE author = ? AND name = ? ORDER BY seq",
                (rs, rowNum) -> new Point(rs.getInt("x"), rs.getInt("y")),
                author, name);
        return new Blueprint(author, name, points);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<String> names = jdbc.query(
                "SELECT name FROM blueprints WHERE author = ?",
                (rs, rowNum) -> rs.getString("name"),
                author);
        if (names.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        Set<Blueprint> result = new HashSet<>();
        for (String name : names) {
            result.add(getBlueprint(author, name));
        }
        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        List<String[]> keys = jdbc.query(
                "SELECT author, name FROM blueprints",
                (rs, rowNum) -> new String[] { rs.getString("author"), rs.getString("name") });
        Set<Blueprint> result = new HashSet<>();
        for (String[] key : keys) {
            try {
                result.add(getBlueprint(key[0], key[1]));
            } catch (BlueprintNotFoundException e) {
                // La fila fue borrada entre ambas consultas; se omite.
            }
        }
        return result;
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        if (!exists(author, name)) {
            throw new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name));
        }
        Integer nextSeq = jdbc.queryForObject(
                "SELECT COALESCE(MAX(seq), -1) + 1 FROM blueprint_points WHERE author = ? AND name = ?",
                Integer.class, author, name);
        jdbc.update("INSERT INTO blueprint_points(author, name, seq, x, y) VALUES (?,?,?,?,?)",
                author, name, nextSeq, x, y);
    }
}
