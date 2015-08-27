package bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.monitor.ServerInfo;

import play.Play;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;


@Configuration
public class SpringMongoConfig {

	public static ServerInfo mongoInfo;
	public static String server = Play.application().configuration().getString("mongo.server");
	public static int port = Play.application().configuration().getInt("mongo.port");
	public static String dbName = Play.application().configuration().getString("mongo.fb.page.name");
	
	public @Bean
	MongoDbFactory mongoDbFactory() throws Exception {
		String server = Play.application().configuration().getString("mongo.server");
		int port = Play.application().configuration().getInt("mongo.port");
		MongoClientOptions.Builder mongoOptions = MongoClientOptions.builder();
		ServerAddress address = new ServerAddress(server, port);
		WriteConcern writeConcern = new WriteConcern(1, 0, false, false);
		mongoOptions.writeConcern(writeConcern);
		return null;
		//return new ThreadLocalDbNameMongoDbFactory(new MongoClient(address,mongoOptions.build()), getDatabaseName());
	}
 
	public @Bean
	MongoTemplate mongoTemplate() throws Exception {
 
		MongoTemplate mongoTemplate =new MongoTemplate(mongoDbFactory());
	
		//mongoInfo = new ServerInfo(mongo);
		return mongoTemplate;
 
	}
	

	public String getDatabaseName() {
		return dbName;
	}
}
