package bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import play.Logger;
import play.Play;
import play.api.inject.ApplicationLifecycle;
import play.libs.F;


/**
 * Data Store configuration. DS is short for data store
 * 
 * @author misael
 *
 */
public class DS {

	public static MongoOperations mop = null;

	/**
	 * Intantiate a connection to mongodb
	 */
	public static void init(){
		// TODO figure out how to authenticate a user
        ApplicationContext ctx = null;
        try {
            Logger.debug(Play.application().configuration().getString("mongo.config"));
            ctx = new AnnotationConfigApplicationContext(Class.forName(Play.application().configuration().getString("mongo.config")));
            Logger.debug(Class.forName(Play.application().configuration().getString("mongo.config")).toString());
        } catch (ClassNotFoundException e) {
            Logger.error("Erro no DS do mongo");
            System.exit(1);
        }
        if(mop == null){
			mop = (MongoOperations)ctx.getBean("mongoTemplate");
		}
	}

}
