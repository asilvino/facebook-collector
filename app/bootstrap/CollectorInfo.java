package bootstrap;

import java.util.ArrayList;
import java.util.List;

import akka.actor.UntypedActor;
import controllers.FacebookCollector;

/**
 * Created by alvaro.joao.silvino on 20/08/2015.
 */
public class CollectorInfo extends UntypedActor {

    public enum Moment {
        ALL("ALL"),
        RECENT("RECENT");

        String name;
        private Moment(String name){
            this.name = name;
        }
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Moment tag : Moment.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
    }

    public CollectorInfo(){

    }

    public void onReceive(Object message){
        if (message instanceof Moment) {
            FacebookCollector.collect((Moment)message);
            getSender().tell(message, getSelf());
        } else {
            unhandled(message);
        }
    }
}
