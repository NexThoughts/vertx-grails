package com.vertx

import io.vertx.core.Vertx;

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

    }


    def two() {

    }

}
