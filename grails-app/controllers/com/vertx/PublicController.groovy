package com.vertx

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

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

    def one() {
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


    def two() {

    }

}
