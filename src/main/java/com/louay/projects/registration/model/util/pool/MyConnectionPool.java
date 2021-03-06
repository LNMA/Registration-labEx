package com.louay.projects.registration.model.util.pool;

import com.louay.projects.registration.model.entity.Schedule;
import com.louay.projects.registration.model.util.queue.MyList;
import com.louay.projects.registration.model.util.queue.MyQueue;

import java.sql.*;

public class MyConnectionPool {
    private MyList<ConnectionWrapper> connection;
    private ConnectionWrapper wrapper;
    private String url;
    private String username;
    private String password;
    private static MyConnectionPool myConnectionPool = null;


    public MyConnectionPool(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        connection = new MyQueue<>(10);
    }

    public ConnectionWrapper getConnection() throws SQLException {
        if (this.connection.isEmpty()) {
            return new ConnectionWrapper(DriverManager.getConnection(url, username, password));
        } else {
            ConnectionWrapper connectionWrapper = this.connection.dequeue();
            if (connectionWrapper.isAlive()) {
                return connectionWrapper;
            } else {
                connectionWrapper.getConnection().close();
                return getConnection();
            }
        }
    }

    public void release(ConnectionWrapper connectionWrapper){
        this.connection.enqueue(connectionWrapper);
    }

    public static MyConnectionPool getMyPooling (String url, String username, String password){
        if (MyConnectionPool.myConnectionPool == null){
               return MyConnectionPool.myConnectionPool = new MyConnectionPool( url,  username,  password);
        }else {
            return MyConnectionPool.myConnectionPool;
        }
    }

    public ResultSet selectResult(String query, String...key) {
        ResultSet resultSet = null;
        try {
            this.wrapper = this.getConnection();
            PreparedStatement preparedStatement = this.wrapper.getConnection().prepareStatement(query);
            for (int i = 0; i < key.length; i++) {
                    preparedStatement.setString((i+1), key[i]);
            }
            resultSet = preparedStatement.executeQuery();
            this.release(this.wrapper);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return resultSet;
    }

    public int updateQuery(String query,Object...objects) {
        int result = 0;
        try {
            this.wrapper = this.getConnection();
            PreparedStatement update = this.wrapper.getConnection().prepareStatement(query);
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof String) {
                    update.setString((i + 1), (String) objects[i]);
                } else {
                    if (objects[i] instanceof Integer) {
                        update.setInt((i + 1), (Integer) objects[i]);
                    } else {
                        if (objects[i] instanceof java.sql.Date) {
                            update.setDate((i + 1), (java.sql.Date) objects[i]);
                        } else {
                            if (objects[i] instanceof Long) {
                                update.setLong((i + 1), (Long) objects[i]);
                            }
                        }
                    }
                }
            }
            result = update.executeUpdate();
            this.release(this.wrapper);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
