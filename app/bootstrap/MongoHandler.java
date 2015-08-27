package bootstrap;

import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

import com.mongodb.MongoClient;



public class MongoHandler {

    private static MongoOperations mop = null;
    private static MongoClient client = null;

    private static MongoHandler instance = null;

    /**
     * Intantiate a connection to mongodb
     */

    public MongoHandler() {
        init();
    }


    public static MongoHandler getInstance(String dbName) {
        if(dbName!=null){
            ///ThreadLocalDbNameMongoDbFactory.clearDefaultNameForCurrentThread();
       //     ThreadLocalDbNameMongoDbFactory.setDefaultNameForCurrentThread(dbName);
        }

        return (instance == null ? instance = new MongoHandler() : instance);
    }
    public static MongoHandler getInstance() {

        //ThreadLocalDbNameMongoDbFactory.clearDefaultNameForCurrentThread();
        //ThreadLocalDbNameMongoDbFactory.setDefaultNameForCurrentThread(null);


        return (instance == null ? instance = new MongoHandler() : instance);
    }

    @SuppressWarnings("resource")
    public static void init(){


        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
        if(mop == null){
            mop = (MongoOperations)ctx.getBean("mongoTemplate");

        }

    }

    /**
     * Gets the time from mongodb server
     * @return the time from the machine hosting the mongodb server
     */
    public static Date getServerTime(){
        try{
            return ((Date) SpringMongoConfig.mongoInfo.getServerStatus().get("localTime"));
        }catch(Exception e){
            return new Date();
        }
    }

    public MongoOperations getMongo(){
        return mop;
    }

    public MongoClient getClient(){
        return client;
    }


}
