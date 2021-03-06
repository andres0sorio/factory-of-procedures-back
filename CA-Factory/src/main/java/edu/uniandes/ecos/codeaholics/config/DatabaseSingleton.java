/** Copyright or License
 *
 */

package edu.uniandes.ecos.codeaholics.config;
/**
 * Package: config
 *
 * Class: DatabaseSingleton DatabaseSingleton.java
 * 
 * Original Author: @author AOSORIO
 * 
 * Description: Database singleton 
 * 
 * Implementation: Configuration taken from DatabaseConfig
 *
 * Created: Jun 8, 2016 8:15:14 AM
 * 
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import edu.uniandes.ecos.codeaholics.main.App;

public class DatabaseSingleton {

	// Atributos
	private static DatabaseSingleton instance      = null;
	private static MongoClient       mongoClient   = null;
	private static MongoDatabase     mongoDatabase = null;

	/**
	 * contructor de la base de datos, crea la base de datos replica o local dependientdo de la seleccion 
	 */
	protected DatabaseSingleton() {
		
		DatabaseConfig dbConf = new DatabaseConfig(App.CONFIG_FILE);
		
		String env = dbConf.getDbEnv();
		
		if (env.equals("replica")){
			mongoClient = new MongoClient(dbConf.getDbServerAdresses());
			mongoDatabase = mongoClient.getDatabase(dbConf.getDbName());
		} else if (env.equals("local")) {
			mongoClient = new MongoClient(dbConf.getDbServerUrl(), Integer.parseInt(dbConf.getDbPort()));
			mongoDatabase = mongoClient.getDatabase(dbConf.getDbName());
		}
		
	}

	// Metodos
	/**
	 * crea una instancia de la base de datos
	 * 
	 * @return instancia de la base de datos
	 */
	public static DatabaseSingleton getInstance() {
		if (instance == null) {
			instance = new DatabaseSingleton();
		}
		return instance;
	}

	public MongoDatabase getDatabase() {
		return mongoDatabase;
	}

}
