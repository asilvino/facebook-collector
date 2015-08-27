package bootstrap;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.monitor.ServerInfo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import models.BeforeSaveListener;
import play.Logger;
import play.Play;

/**
 * Mongodb Configuration
 * @author misael
 *
 */
@Configuration
public class MongoConf extends AbstractMongoConfiguration{

	private static Mongo mongo = null;
	
	private static boolean online = false;
	
	public static ServerInfo mongoInfo;
	
	public static boolean isOnline(){
		return online;
	}

	public static void setOnline(boolean online) {
		MongoConf.online = online;
	}
	
	public static String testCon(){
		try { // try to connect to server and get database names. This is a way to force connection timeout synchronously
			String ret = mongo.getDatabaseNames().toString();
			online = true;
			return ret;
		} catch (MongoException e) {
			Logger.error(e.getMessage());
			online = false;
			return "No mongo servers connections";
		} catch (Exception e){
			Logger.error(e.getMessage());
			online = false;
			return "No mongo servers connections";
		}
	}
	
	public static void disconnect(){
		if(mongo != null){
			mongo.close();
		}
	}

	@Override
	protected String getDatabaseName() {
		return Play.application().configuration().getString("connections.mongo.name");
	}
    @Bean
    public BeforeSaveListener beforeSaveListener() {
        return new BeforeSaveListener();
    }

	@Override
	@Bean
	public Mongo mongo() throws Exception {
		
		List<String> servers = Play.application().configuration().getStringList("connections.mongo.servers");
		
		String replicaSet = Arrays.toString(servers.toArray()).replace("[", "").replace("]", ""); 
		boolean slaveOk = Play.application().configuration().getBoolean("connections.mongo.slave");
		boolean fsync = false;
		boolean jornaling = false;
		int writeNumber = 1;
		int connectionsPerHost = 50;
		boolean useSSL = false;

		ServerAddress addr = new ServerAddress();
		List<ServerAddress> addresses = new ArrayList<ServerAddress>();
		int port =0;
		String host = new String();
		if ( replicaSet == null ){
			throw new UnknownHostException("Please provide hostname");
		}
		replicaSet = replicaSet.trim();
		if ( replicaSet.length() == 0 ){
			throw new UnknownHostException("Please provide hostname");
		}
		StringTokenizer tokens = new StringTokenizer(replicaSet, ",");
		while(tokens.hasMoreTokens()){
			String token = tokens.nextToken();
			
			int idx = token.indexOf( ":" );
			if ( idx > 0 ){               
				port = Integer.parseInt( token.substring( idx + 1 ) );
				host = token.substring( 0 , idx ).trim();
			}
			addr = new ServerAddress(host.trim(), port);
			addresses.add(addr);
		}

		//  mongo options
		MongoClientOptions.Builder mongoOptions = MongoClientOptions.builder();
		if (useSSL){
			mongoOptions.socketFactory(SSLSocketFactory.getDefault());
		}

		WriteConcern writeConcern = new WriteConcern(writeNumber, 0, fsync, jornaling);

		mongoOptions.connectionsPerHost(connectionsPerHost);
		mongoOptions.writeConcern(writeConcern);
		mongoOptions.maxWaitTime(5000);
		mongoOptions.connectTimeout(5000);
		mongoOptions.autoConnectRetry(true);
		mongoOptions.socketKeepAlive(true);
		mongoOptions.threadsAllowedToBlockForConnectionMultiplier(50);

		mongo = new MongoClient(addresses, mongoOptions.build());
		if(slaveOk){
			mongo.setReadPreference(ReadPreference.secondaryPreferred());
		}

		try { // try to connect to server and get database names. This is a way to force connection timeout synchronously
			List<String> strings = mongo.getDatabaseNames();
			if(strings != null && strings.size() > 0){
				online = true;
			}
			Logger.debug(strings  + "");
		} catch (MongoException e) {// if connection times out that means that the server cant be reached
			Logger.error(e.getMessage());
		}
		
		mongoInfo = new ServerInfo(mongo);

		return mongo;
	}
}