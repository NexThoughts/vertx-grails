import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.NetServer;

class BootStrap {

    def init = { servletContext ->

//        Vertx vertx = Vertx.vertx();
//
////        def vertx = Vertx.vertx([
////                workerPoolSize:40
////        ])
//
//        def server = vertx.createNetServer()
//        server.listen(0, "localhost", { res ->
//            if (res.succeeded()) {
//                println("***** Server is now listening on actual port: ${server.actualPort()}")
//            } else {
//                println("***** Failed to bind!")
//            }
//        })
    }


    def destroy = {


    }
}
