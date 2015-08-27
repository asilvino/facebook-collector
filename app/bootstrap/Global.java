package bootstrap; /**
 * Created by Alvaro on 18/03/2015.
 */
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import controllers.FacebookCollector;
import models.Page;
import models.User;
import play.*;
import play.api.mvc.EssentialFilter;
import play.filters.csrf.CSRFFilter;
import play.libs.Akka;
import play.libs.F.*;
import play.mvc.*;
import play.mvc.Http.*;
import scala.concurrent.duration.Duration;
import service.MongoService;

import static play.mvc.Results.*;

public class Global extends GlobalSettings {

    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
         return new Class[]{CSRFFilter.class};
    }

    public void onStart(Application app) {
        DS.init();

        createPages();


        createIndexForPost();
        ActorRef instance = Akka.system().actorOf(Props.create(CollectorInfo.class),"collector");

        Akka.system().scheduler().schedule(
            Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
            Duration.create(30, TimeUnit.DAYS),     //Frequency 30 minutes
            instance,
            CollectorInfo.Moment.ALL,
            Akka.system().dispatcher(),
            null
        );

        Akka.system().scheduler().schedule(
            Duration.create(40, TimeUnit.HOURS), //Initial delay 0 milliseconds
            Duration.create(30, TimeUnit.HOURS),     //Frequency 30 minutes
            instance,
            CollectorInfo.Moment.RECENT,
            Akka.system().dispatcher(),
            null
        );
        Logger.info("Application has started");
    }

    private void createIndexForPost() {
        try {
            DS.mop.createCollection("post");
            DBObject textIndex = new BasicDBObject("message", "text");
            textIndex.put("link","text");
            textIndex.put("name","text");
            DS.mop.getCollection("post").createIndex(textIndex);
        }catch (Exception e){
            Logger.debug("error on index post"+e.getMessage());
        }
    }

    private void createPages() {
        for (FacebookCollector.FacebookPages fbpage : FacebookCollector.FacebookPages.values()) {
            Page page = new Page();
            page.setId(fbpage.id);
            page.setTitle(fbpage.name());
            DS.mop.save(page);
        }

    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

    public Promise<Result> onError(RequestHeader request, Throwable t) {
        return Promise.<Result>pure(internalServerError(
                //views.html.static_error.render(t)
                views.html.static_error.render()

        ));
    }

    public Promise<Result> onHandlerNotFound(RequestHeader request) {
        return Promise.<Result>pure(notFound(
                // views.html.static_notFound.render(request.uri())
                views.html.static_notFound.render()

        ));
    }
    public Promise<Result> onBadRequest(RequestHeader request, String error) {
        return Promise.<Result>pure(badRequest("Don't try to hack the URI!"));
    }

    //For CORS
    private class ActionWrapper extends Action.Simple {
         public ActionWrapper(Action<?> action) {
             this.delegate = action;
         }

         @Override
         public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
             Promise<Result> result = this.delegate.call(ctx);
             Http.Response response = ctx.response();
             response.setHeader("Access-Control-Allow-Origin", "*");
             return result;
         }
    }

    @Override
    public Action<?> onRequest(Http.Request request,
                                java.lang.reflect.Method actionMethod) {
         return new ActionWrapper(super.onRequest(request, actionMethod));
    }



}