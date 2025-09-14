package group13.demo1.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteAccomplishmentDAO implements IAccomplishmentDAO
{
    private final Connection connection;

    public SqliteAccomplishmentDAO()
    {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable()
    {
        String query = """
                CREATE TABLE IF NOT EXISTS Accomplishment (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    label TEXT NOT NULL,
                    completed INTEGER DEFAULT 0
                )
                """;
        try (Statement stmt = connection.createStatement())
        {
            stmt.execute(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void addAccomplishment(Accomplishment accomplishment) {}

    @Override
    public void updateAccomplishment(Accomplishment accomplishment) {}

    @Override
    public void deleteAccomplishment(Accomplishment accomplishment) {}

    @Override
    public Accomplishment getAccomplishmentById (int id) {return null;}

    @Override
    public List<Accomplishment> getAccomplishments() {return new ArrayList<>();}

    @Override
    public List<Accomplishment> getAccomplishmentsByUsername (String username) {return new ArrayList<>();}
}
