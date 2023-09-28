package ru.job4j.grabber;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try (InputStream in = new FileInputStream("db/liquibase.properties")) {
            cfg.load(in);
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        Timestamp time = timeConverterToTimestamp(post.getCreated());
        try (PreparedStatement statement =
                     cnn.prepareStatement("INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING;", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, time);
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> post = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post;")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post.add(postCreator(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            timeConverterToLocalDateTime(resultSet.getTimestamp("created")))
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post WHERE id = ?;")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = postCreator(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            timeConverterToLocalDateTime(resultSet.getTimestamp("created"))
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post postCreator(int id, String title, String link, String description, LocalDateTime created) {
        return new Post(id, title, link, description, created);
    }

    private Timestamp timeConverterToTimestamp(LocalDateTime ldt) {
        return Timestamp.valueOf(ldt);
    }

    private LocalDateTime timeConverterToLocalDateTime(Timestamp ts) {
        return ts.toLocalDateTime();
    }
}