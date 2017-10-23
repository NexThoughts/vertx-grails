package com.vertx

import io.vertx.core.Vertx;
import io.vertx.ext.web.*;
import io.vertx.ext.jdbc.*;
import io.vertx.core.json.*;
import io.vertx.ext.sql.*;

class PublicController {

    def index() {
        def vertx = Vertx.vertx([
                workerPoolSize: 40
        ])

        vertx.createHttpServer().requestHandler({ req ->
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x!")
        }).listen(8084)

        render "Success"
    }

    def web() {
        def vertx = Vertx.vertx([
                workerPoolSize: 40
        ])

        def server = vertx.createHttpServer()

        def router = Router.router(vertx)

        router.route().handler({ routingContext ->

            // This handler will be called for every request
            def response = routingContext.response()
            response.putHeader("content-type", "text/plain")

            // Write to the response and end it
            response.end("Hello World from Vert.x-Web!")
        })

        server.requestHandler(router.&accept).listen(8085)
        render "Success - 1"

    }


    def jdbc() {
        def vertx = Vertx.vertx([
                workerPoolSize: 40
        ])
        println "-----0-----------"

        JsonObject config = new JsonObject()
//                .put("url", "jdbc:mysql:localhost:demo_lending?autoreconnect=true")
                .put("url", "jdbc:mysql://localhost:3306/demo_lending?autoreconnect=true")
//        url = "jdbc:mysql://localhost:3306/demo_lending?autoreconnect=true"
                .put("user", "root")
                .put("password", "nextdefault")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("max_pool_size", 30);

        SQLClient client = JDBCClient.createShared(vertx, config);
        println "-----1-----------" + client

        client.getConnection({ res ->

            println "-----2-----------" + res.properties
            if (res.failed()) {
                println "-----2 Failed-----------"
                println(res.cause().getMessage())
                return
            }
            if (res.succeeded()) {
                println "-----2.1-----------"
                SQLConnection connection = res.result();
                println "-----2.2-----------"
                connection.query("SELECT * FROM user", { res2 ->
                    println "-----2.3-----------"
                    if (res2.succeeded()) {
                        def rs = res2.result();
                        rs.results.each { line ->
                            println('----------' + groovy.json.JsonOutput.toJson(line))
                        }

                    }
                });
            } else {

                println "---- Failed to get connection ------"

                // Failed to get connection - deal with it
            }
        });
        println "-----3-----------"

        render "Success - 2"
    }


    def jdbc1() {
        def vertx = Vertx.vertx([
                workerPoolSize: 40
        ])
        println "---1--0-----------"

        JsonObject config = new JsonObject()
                .put("url", "jdbc:mysql://localhost:3306/demo_lending?autoreconnect=true")
                .put("user", "root")
                .put("password", "nextdefault")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("max_pool_size", 30);

        def client = JDBCClient.createShared(vertx, config);
        println "-----1-----------" + client

        client.getConnection({ conn ->

            def connection = conn.result()

            // query some data with arguments
            connection.query("select * from user where id < 20", { rs ->
                if (rs.failed()) {
                    println("Cannot retrieve the data from the database")
                    rs.cause().printStackTrace()
                    return
                }

                rs.result().results.each { line ->
                    println("-----****---- : " + groovy.json.JsonOutput.toJson(line))
                }

                // and close the connection
                connection.close({ done ->
                    if (done.failed()) {
                        throw new java.lang.RuntimeException(done.cause())
                    }

                })
            })
        })
        println "----1-3-----------"
        render "Success - 3"
    }
}
