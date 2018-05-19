package com.john.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.text.MessageFormat;
import java.util.List;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {

        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:Sqlite.db")
                .put("driver_class", "org.sqlite.JDBC");

        JDBCClient client = JDBCClient.createShared(vertx, config);


        String sql = MessageFormat.format("SELECT * FROM {0} WHERE id = ? ", "person");
        JsonArray params = new JsonArray();
        params.add(1);
        client.queryWithParams(sql,params,res ->{
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray array = new JsonArray();
                rows.forEach(va -> {
                    System.out.println(va.getInstant("id"));
                    System.out.println(va.getString("name"));
                });
            } else {
            }
        });

    }
}
