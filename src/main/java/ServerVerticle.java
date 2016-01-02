import data.Whisky;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wangg5 on 12/29/15.
 */
public class ServerVerticle extends AbstractVerticle {

    private Map<Integer, Whisky> products = new LinkedHashMap<>();

    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);
        createSomeData();

        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first vert.x 3 web app</h1>");
        });

        // Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));

        //get all whiskies
        router.get("/api/whiskies").handler(this::getAll);

        //create a whisky
        router.route("/api/whiskies*").handler(BodyHandler.create());
        router.post("/api/whiskies").handler(this::addOne);

        //delete a whisky
        router.delete("/api/whiskies/:id").handler(this::deleteOne);

        //create the http server and pass the accept method to the request handler
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        //Retrieve the port from the configuration
                        // default to 8080
                        config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    private void createSomeData() {
        Whisky bowmore = new Whisky("Bowmore 15 years Laimrig", "Scotland, Islay");
        products.put(bowmore.getId(), bowmore);
        Whisky talisker = new Whisky("Talisker 57 degree North", "Scotland, Island");
        products.put(talisker.getId(), talisker);
    }

    //this method will stay in the context handler
    private void getAll(RoutingContext context) {
        context.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(products.values()));
    }

    //create a whisky
    private void addOne(RoutingContext routingContext) {
        final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(),
                Whisky.class);
        products.put(whisky.getId(), whisky);
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whisky));
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            products.remove(idAsInteger);
        }
        routingContext.response().setStatusCode(204).end();
    }
}
