package group13.demo1.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) implementation used to manage {@link Accomplishment} entities using
 * an SQLite database.
 * <p>
 * Provides CRUD operations: adding, updating, deleting, retrieving accomplishments.
 */
public class SqliteAccomplishmentDAO implements IAccomplishmentDAO
{
    private final Connection connection;

    public SqliteAccomplishmentDAO()
    {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    void createTable()
    {
        String query = """
                CREATE TABLE IF NOT EXISTS Accomplishment (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    label TEXT NOT NULL,
                    completed INTEGER DEFAULT 0
                )
                """;
        try (Statement statement = connection.createStatement())
        {
            statement.execute(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void addAccomplishment(Accomplishment accomplishment)
    {
        String query = "INSERT INTO accomplishment (username, label, completed) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query))
//        (Connection connection = DriverManager.getConnection(url);
        {
            statement.setString(1, accomplishment.getUsername());
            statement.setString(2, accomplishment.getLabel());
            statement.setBoolean(3, accomplishment.isCompleted());
            statement.executeUpdate();
        } catch (SQLException e)
        {
            System.out.println("Add accomplishment failed" + e.getMessage());
        }
    }

    @Override
    public void updateAccomplishment(Accomplishment accomplishment)
    {
        String query = "UPDATE accomplishment SET username = ?, label = ?, completed = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, accomplishment.getUsername());
            statement.setString(2, accomplishment.getLabel());
            statement.setBoolean(3, accomplishment.isCompleted());
            statement.setInt(4, accomplishment.getId());
            statement.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAccomplishment(Accomplishment accomplishment)
    {
        String query = "DELETE FROM accomplishment WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, accomplishment.getId());
            statement.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Accomplishment getAccomplishmentById (int id)
    {
        String query = "SELECT * FROM accomplishment WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next())
            {
                return new Accomplishment(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("label"),
                        result.getInt("completed") == 1
                );
            }
        } catch (SQLException e)
        {
            System.out.println("Getting accomplishment by ID failed" + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Accomplishment> getAccomplishments()
    {
        List<Accomplishment> list = new ArrayList<>();
        String query = "SELECT * FROM accomplishment";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query))
        {
            while (result.next())
            {
                list.add(new Accomplishment(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("label"),
                        result.getInt("completed") == 1
                ));
            }
        } catch (SQLException e)
        {
            System.out.println("Getting accomplishments failed" + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Accomplishment> getAccomplishmentsByUsername(String username)
    {
        List<Accomplishment> list = new ArrayList<>();
        String query = "SELECT * FROM accomplishment WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            while (result.next())
            {
                list.add(new Accomplishment(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("label"),
                        result.getInt("completed") == 1
                ));
            }
        } catch (SQLException e)
        {
            System.out.println("Getting accomplishments by username failed" + e.getMessage());
        }
        return list;
    }
}