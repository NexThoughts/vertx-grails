package com.vertx

import io.vertx.core.Vertx;
import io.vertx.ext.web.*;
import io.vertx.ext.jdbc.*;
import io.vertx.core.json.*;
import io.vertx.ext.sql.*;
import io.vertx.core.http.*;
import io.vertx.ext.web.handler.*;

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
                .put("user", "demo")
                .put("password", "demo")
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
                .put("user", "demo")
                .put("password", "demo")
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

    private def productList = new HashMap<>();

    def route1() {

        def vertx = Vertx.vertx([
                workerPoolSize: 40
        ])

        setUpInitialData()

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/products/:productID").handler(this.&handleGetProduct);
        router.put("/products/:productID").handler(this.&handleAddProduct);
        router.get("/products").handler(this.&handleListProducts);

        vertx.createHttpServer().requestHandler(router.&accept).listen(8086);

        render "Success - 4"
    }

    private void handleGetProduct(RoutingContext routingContext) {
        String productID = routingContext.request().getParam("productID");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject product = productList.get(productID);
            if (product == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(product.encodePrettily());
            }
        }
    }

    private void handleAddProduct(RoutingContext routingContext) {
        String productID = routingContext.request().getParam("productID");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject product = routingContext.getBodyAsJson();
            if (product == null) {
                sendError(400, response);
            } else {
                productList.put(productID, product);
                response.end();
            }
        }
    }

    private void handleListProducts(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();
        productList.each{k, v ->
            arr.add(v)
        }
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void setUpInitialData() {
        addProduct(new JsonObject().put("id", "prod3568").put("name", "Egg Whisk").put("price", "3.99").put("weight", "150"));
        addProduct(new JsonObject().put("id", "prod7340").put("name", "Tea Cosy").put("price", "5.99").put("weight", "100"));
        addProduct(new JsonObject().put("id", "prod8643").put("name", "Spatula").put("price", "1.00").put("weight", "80"));
    }

    private void addProduct(JsonObject product) {
        productList.put(product.getString("id"), product);
    }


    def route2() {
        def route1 = router.route("/some/path/").handler({ routingContext ->

            def response = routingContext.response()
            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.setChunked(true)

            response.write("route1\n")

            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(5000, { tid ->
                routingContext.next()
            })
        })

        def route2 = router.route("/some/path/").handler({ routingContext ->

            def response = routingContext.response()
            response.write("route2\n")

            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(5000, { tid ->
                routingContext.next()
            })
        })

        def route3 = router.route("/some/path/").handler({ routingContext ->

            def response = routingContext.response()
            response.write("route3")

            // Now end the response
            routingContext.response().end()
        })

        render "Success - 5"
    }
}
