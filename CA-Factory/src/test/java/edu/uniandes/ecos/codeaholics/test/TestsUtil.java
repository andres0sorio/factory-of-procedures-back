/** Copyright or License
 *
 */

package edu.uniandes.ecos.codeaholics.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uniandes.ecos.codeaholics.config.Constants;
import edu.uniandes.ecos.codeaholics.config.DataBaseUtil;
import edu.uniandes.ecos.codeaholics.config.DatabaseSingleton;
import edu.uniandes.ecos.codeaholics.config.ExternalSvcInvoker;
import edu.uniandes.ecos.codeaholics.config.GeneralUtil;
import edu.uniandes.ecos.codeaholics.config.Routes;
import edu.uniandes.ecos.codeaholics.main.App;
import edu.uniandes.ecos.codeaholics.persistence.Activity;
import edu.uniandes.ecos.codeaholics.persistence.Citizen;
import edu.uniandes.ecos.codeaholics.persistence.Dependency;
import edu.uniandes.ecos.codeaholics.persistence.FormField;
import edu.uniandes.ecos.codeaholics.persistence.Functionary;
import edu.uniandes.ecos.codeaholics.persistence.History;
import edu.uniandes.ecos.codeaholics.persistence.Mayoralty;
import edu.uniandes.ecos.codeaholics.persistence.Procedure;
import edu.uniandes.ecos.codeaholics.persistence.ProcedureRequest;
import edu.uniandes.ecos.codeaholics.persistence.RequiredUpload;
import edu.uniandes.ecos.codeaholics.persistence.Session;

/**
 * Package: edu.uniandes.ecos.codeaholics.test
 *
 * Class: TestsUtil TestsUtil.java
 * 
 * Original Author: @author AOSORIO
 * 
 * Description: Utilities for running tests
 * 
 * Implementation: [Notes on implementation]
 *
 * Created: Aug 14, 2016 5:39:16 PM
 * 
 */
public class TestsUtil {

	private static Logger logger = LogManager.getRootLogger();

	private static String birthDateStr = "10-01-1990";

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyyy");

	private static String citizenSalt;

	/**
	 * add citizen to db for testing purposes. if it already exists, just update
	 * 
	 * @param pName
	 * @param pLastName1
	 * @param pEmail
	 * @param pPwd
	 */
	public static void addCitizen(String pName, String pLastName1, String pLastName2, String pEmail, String pPwd) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.CITIZEN_COLLECTION);

		Citizen citizen = new Citizen();
		citizen.setName(pName);
		citizen.setLastName1(pLastName1);
		citizen.setLastName2(pLastName2);
		citizen.setIdentification(1234567890);
		citizen.setEmail(pEmail);
		citizen.setPassword(pPwd);
		citizen.setUserProfile(Constants.CITIZEN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists: " + pName);
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	/**
	 * @param pEmail
	 * @return
	 */
	public static String getCitizenSalt( String pEmail ) {
		
		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			return null;
		} else {
			Document citizenDoc = documents.get(0);
			citizenSalt = (String) citizenDoc.get("salt");
		}
		
		return citizenSalt;
		
	}
	
	/**
	 * @param pEmail
	 * @return
	 */
	public static String getFunctionarySalt( String pEmail ) {
		
		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.FUNCTIONARY_COLLECTION);

		if (documents.isEmpty()) {
			return null;
		} else {
			Document citizenDoc = documents.get(0);
			citizenSalt = (String) citizenDoc.get("salt");
		}
		
		return citizenSalt;
		
	}
	
	/**
	 * Cleanup DB of Test users
	 * 
	 * @param pEmail
	 */
	public static void removeCitizen(String pEmail) {

		Document user = new Document();
		user.append("email", pEmail);
		logger.info("Removing user with email ... " + pEmail);
		DataBaseUtil.delete(user, Constants.CITIZEN_COLLECTION);

	}

	/**
	 * Cleanup DB of Test users
	 * 
	 * @param pEmail
	 */
	public static void removeFunctionary(String pEmail) {

		Document user = new Document();
		user.append("email", pEmail);
		logger.info("Removing user with email ... " + pEmail);
		DataBaseUtil.delete(user, Constants.FUNCTIONARY_COLLECTION);

	}
	
	/**
	 * Create a mock session for a specific user
	 * 
	 * @param pEmail
	 * @param pProfile
	 * @param pToken
	 * @param pSalt
	 */
	public static void addSession(String pEmail, String pProfile, String pToken, String pSalt) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.SESSION_COLLECTION);

		Session session = new Session();
		session.setEmail(pEmail);
		session.setUserProfile(pProfile);
		session.setToken(pToken);
		session.setSalt(pSalt);

		Document prevSession = new Document();
		prevSession.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(prevSession, Constants.SESSION_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(session.toDocument());
		} else {
			logger.info("session alreadery exists for: " + pEmail);
			collection.findOneAndDelete(prevSession);
			collection.insertOne(session.toDocument());
		}

	}

	/**
	 * 
	 */
	public static void clearAllCollections() {

		logger.info("clearing all existing collections in the default DB");

		ArrayList<String> collections = new ArrayList<String>();
		collections.add(Constants.CITIZEN_COLLECTION);
		collections.add(Constants.FUNCTIONARY_COLLECTION);
		collections.add(Constants.MAYORALTY_COLLECTION);
		collections.add(Constants.PROCEDURE_COLLECTION);
		collections.add(Constants.PROCEDUREREQUEST_COLLECTION);
		collections.add(Constants.SESSION_COLLECTION);

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection;

		Iterator<String> itrColl = collections.iterator();

		while (itrColl.hasNext()) {
			String collectionName = itrColl.next();
			collection = dbOne.getCollection(collectionName);
			collection.drop();
			logger.info("Collection " + collectionName + " dropped");
		}

	}

	// add citizen
	public static void addCitizenUno() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.CITIZEN_COLLECTION);

		Citizen citizen = new Citizen();
		citizen.setName("Andr\u00E9s");
		citizen.setLastName1("Osorio");
		citizen.setLastName2("Vargas");
		citizen.setIdentification(1234567890);
		citizen.setEmail("aosorio@uniandes.edu.co");
		citizen.setPassword("12345678");
		citizen.setUserProfile(Constants.CITIZEN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", "aosorio@uniandes.edu.co");
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// add citizen
	public static void addCitizenDos() {
		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.CITIZEN_COLLECTION);

		Citizen citizen = new Citizen();
		citizen.setName("Fabian");
		citizen.setLastName1("Hernandez");
		citizen.setLastName2("Schmidt");
		citizen.setIdentification(1234567890);
		citizen.setEmail("f.hernandez@uniandes.edu.co");
		citizen.setPassword("12345678");
		citizen.setUserProfile(Constants.CITIZEN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", "f.hernandez@uniandes.edu.co");
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// add citizen
	public static void addCitizenTres() {
		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.CITIZEN_COLLECTION);

		Citizen citizen = new Citizen();
		citizen.setName("Jheison");
		citizen.setLastName1("Rodriguez");
		citizen.setLastName2("Borja");
		citizen.setIdentification(1234567890);
		citizen.setEmail("jl.rodriguez@uniandes.edu.co");
		citizen.setPassword("12345678");
		citizen.setUserProfile(Constants.CITIZEN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", "jl.rodriguez@uniandes.edu.co");
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// add citizen
	public static void addCitizenCuatro() {
		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.CITIZEN_COLLECTION);

		Citizen citizen = new Citizen();
		citizen.setName("David");
		citizen.setLastName1("Martinez");
		citizen.setLastName2("Salcedo");
		citizen.setIdentification(1234567890);
		citizen.setEmail("df.martinez1@uniandes.edu.co");
		citizen.setPassword("12345678");
		citizen.setUserProfile(Constants.CITIZEN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", "df.martinez1@uniandes.edu.co");
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	public static void addCitizenCinco() {
		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.CITIZEN_COLLECTION);

		Citizen citizen = new Citizen();
		citizen.setName("Sebastian");
		citizen.setLastName1("Cardona");
		citizen.setLastName2("Correa");
		citizen.setIdentification(1234567890);
		citizen.setEmail("s.cardona12@uniandes.edu.co");
		citizen.setPassword("12345678");
		citizen.setUserProfile(Constants.CITIZEN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", "s.cardona12@uniandes.edu.co");
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.CITIZEN_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// add Alcaldia uno
	public static void addMayoraltyUno(ArrayList<String> pProcedureList) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection("mayoralty");

		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("Anapoima");
		mayoralty.setAddress("CRA 123 45 1");
		mayoralty.setUrl("https://anapoima.gov.co");
		mayoralty.setPhone("333555888");

		Dependency dependencyUno = new Dependency();
		dependencyUno.setName("Hacienda");

		ArrayList<Functionary> funcionaryUno = new ArrayList<>();
		Functionary funcionarioUno = new Functionary();

		funcionarioUno.setEmail("jvaldez@anapoima.gov.co");
		funcionaryUno.add(funcionarioUno);
		dependencyUno.setFunctionaries(funcionaryUno);

		Dependency dependencyDos = new Dependency();
		dependencyDos.setName("Atenci\u00F3n al Ciudadano");

		ArrayList<Functionary> funcionaryDos = new ArrayList<>();
		Functionary funcionarioDos = new Functionary();

		funcionarioDos.setEmail("acalle@anapoima.gov.co");
		funcionaryDos.add(funcionarioDos);
		dependencyDos.setFunctionaries(funcionaryDos);

		ArrayList<Dependency> dependencies = new ArrayList<>();

		dependencies.add(dependencyUno);
		dependencies.add(dependencyDos);

		mayoralty.setDependencies(dependencies);

		mayoralty.setProcedures(pProcedureList);

		collection.insertOne(mayoralty.toDocument());

	}

	// add Alcaldia dos
	public static void addMayoraltyDos(ArrayList<String> pProcedure) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection("mayoralty");

		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("El Rosal");
		mayoralty.setAddress("CRA 456 78 1");
		mayoralty.setUrl("https://elrosal.gov.co");
		mayoralty.setPhone("99977766");

		Dependency dependencyUno = new Dependency();
		dependencyUno.setName("Hacienda");

		ArrayList<Functionary> funcionaryUno = new ArrayList<>();
		Functionary funcionarioUno = new Functionary();

		funcionarioUno.setEmail("jvaldez@elrosal.gov.co");
		funcionaryUno.add(funcionarioUno);
		dependencyUno.setFunctionaries(funcionaryUno);

		Dependency dependencyDos = new Dependency();
		dependencyDos.setName("Atenci\u00F3n al Ciudadano");

		ArrayList<Functionary> funcionaryDos = new ArrayList<>();
		Functionary funcionarioDos = new Functionary();

		funcionarioDos.setEmail("acalle@elrosal.gov.co");
		funcionaryDos.add(funcionarioDos);
		dependencyDos.setFunctionaries(funcionaryDos);

		ArrayList<Dependency> dependencies = new ArrayList<>();

		dependencies.add(dependencyUno);
		dependencies.add(dependencyDos);

		mayoralty.setDependencies(dependencies);

		mayoralty.setProcedures(pProcedure);

		collection.insertOne(mayoralty.toDocument());

	}

	
	/**
	 *  Add a generic functionary
	 * @param pName
	 * @param pLastName1
	 * @param pLastName2
	 * @param pEmail
	 * @param pPwd
	 * @param pRole
	 */
	public static void addFunctionary(String pName, String pLastName1, String pLastName2, String pEmail, String pPwd, String pProfile) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.FUNCTIONARY_COLLECTION);
		
		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("Anapoima");
		mayoralty.setAddress("CRA 123 45 1");
		mayoralty.setUrl("https://anapoima.gov.co");
		mayoralty.setPhone("333555888");

		Functionary citizen = new Functionary();
		citizen.setName(pName);
		citizen.setLastName1(pLastName1);
		citizen.setLastName2(pLastName2);
		citizen.setIdentification(1234567890);
		citizen.setEmail(pEmail);
		citizen.setPassword(pPwd);
		citizen.setUserProfile(pProfile);
		citizen.setBirthDate(getBirthdate());

		citizen.setMayoralty("MiAlcaldia");
		citizen.setDependency("Hacienda");

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.FUNCTIONARY_COLLECTION);
		
		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}
		
	}

	/**
	 * @param pName
	 * @param pLastName1
	 * @param pEmail
	 * @param pPwd
	 */
	// funcionario1
	public static void addFunctionaryUno(String pName, String pLastName1, String pLastName2, String pEmail,
			String pPwd) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.FUNCTIONARY_COLLECTION);

		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("Anapoima");
		mayoralty.setAddress("CRA 123 45 1");
		mayoralty.setUrl("https://anapoima.gov.co");
		mayoralty.setPhone("333555888");

		Functionary citizen = new Functionary();
		citizen.setName(pName);
		citizen.setLastName1(pLastName1);
		citizen.setLastName2(pLastName2);
		citizen.setIdentification(1234567890);
		citizen.setEmail(pEmail);
		citizen.setPassword(pPwd);
		citizen.setUserProfile(Constants.ADMIN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		citizen.setMayoralty("Anapoima");
		citizen.setDependency("Hacienda");

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.FUNCTIONARY_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// funcionario2
	public static void addFunctionaryDos(String pName, String pLastName1, String pLastName2, String pEmail,
			String pPwd) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.FUNCTIONARY_COLLECTION);

		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("Anapoima");
		mayoralty.setAddress("CRA 123 45 1");
		mayoralty.setUrl("https://anapoima.gov.co");
		mayoralty.setPhone("333555888");

		Functionary citizen = new Functionary();
		citizen.setName(pName);
		citizen.setLastName1(pLastName1);
		citizen.setLastName2(pLastName2);
		citizen.setIdentification(1234567890);
		citizen.setEmail(pEmail);
		citizen.setPassword(pPwd);
		citizen.setUserProfile(Constants.FUNCTIONARY_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		citizen.setMayoralty("Anapoima");
		citizen.setDependency("Atenci\u00F3n al Ciudadano");

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.FUNCTIONARY_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// funcionario2
	public static void addFunctionaryTres(String pName, String pLastName1, String pLastName2, String pEmail,
			String pPwd) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.FUNCTIONARY_COLLECTION);

		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("El Rosal");
		mayoralty.setAddress("CRA 456 78 1");
		mayoralty.setUrl("https://elrosal.gov.co");
		mayoralty.setPhone("99977766");

		Functionary citizen = new Functionary();
		citizen.setName(pName);
		citizen.setLastName1(pLastName1);
		citizen.setLastName2(pLastName2);
		citizen.setIdentification(1234567890);
		citizen.setEmail(pEmail);
		citizen.setPassword(pPwd);
		citizen.setUserProfile(Constants.ADMIN_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		citizen.setMayoralty("El Rosal");
		citizen.setDependency("Hacienda");

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.FUNCTIONARY_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// funcionario4
	public static void addFunctionaryCuatro(String pName, String pLastName1, String pLastName2, String pEmail,
			String pPwd) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.FUNCTIONARY_COLLECTION);

		Mayoralty mayoralty = new Mayoralty();
		mayoralty.setName("El Rosal");
		mayoralty.setAddress("CRA 456 78 1");
		mayoralty.setUrl("https://elrosal.gov.co");
		mayoralty.setPhone("99977766");

		Functionary citizen = new Functionary();
		citizen.setName(pName);
		citizen.setLastName1(pLastName1);
		citizen.setLastName2(pLastName2);
		citizen.setIdentification(1234567890);
		citizen.setEmail(pEmail);
		citizen.setPassword(pPwd);
		citizen.setUserProfile(Constants.FUNCTIONARY_USER_PROFILE);
		citizen.setBirthDate(getBirthdate());

		citizen.setMayoralty("El Rosal");
		citizen.setDependency("Atenci\u00F3n al Ciudadano");

		String[] hash = GeneralUtil.getHash(citizen.getPassword(), "");
		citizen.setPassword(hash[1]);
		citizen.setSalt(hash[0]);

		Document user = new Document();
		user.append("email", pEmail);
		ArrayList<Document> documents = DataBaseUtil.find(user, Constants.FUNCTIONARY_COLLECTION);

		if (documents.isEmpty()) {
			collection.insertOne(citizen.toDocument());
		} else {
			logger.info("user alreadery exists");
			collection.findOneAndDelete(user);
			collection.insertOne(citizen.toDocument());
		}

	}

	// Procedure1
	public static void addProcedureUno(String pCode, String pName, String pMayoralty, String pFunctionary) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDURE_COLLECTION);

		ArrayList<FormField> formFields = new ArrayList<FormField>();
		ArrayList<RequiredUpload> reqDocs = new ArrayList<RequiredUpload>();
		ArrayList<Activity> activities = new ArrayList<Activity>();

		Procedure procedure = new Procedure();
		procedure.setCode(pCode);
		procedure.setName(pName);
		procedure.setMayoralty(pMayoralty);

		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary(pFunctionary);
		activity1.setStatus(Constants.STATUS_PENDING);

		activities.add(activity1);

		procedure.setActivities(activities);

		// Required
		RequiredUpload reqDoc1 = new RequiredUpload();

		reqDoc1.setType("file");
		reqDoc1.setRequired(true);
		reqDoc1.setClassName("form-control");

		reqDoc1.setLabel("C\u00E9dula de Ciudadan\u00EDa");
		reqDoc1.setDescription("Adjunte su c\u00E9dula en formato (png, jpeg)");
		reqDoc1.setName("cedulaAtt");

		reqDocs.add(reqDoc1);

		if (pName.equals("Anapoima")) {

			RequiredUpload reqDoc2 = new RequiredUpload();

			reqDoc2.setType("file");
			reqDoc2.setRequired(true);
			reqDoc2.setClassName("form-control");

			reqDoc2.setLabel("Recibo de pago");
			reqDoc2.setDescription("Adjunte su recibo de pago en formato (png, jpeg)");
			reqDoc2.setName("reciboAtt");

			reqDocs.add(reqDoc2);
		}

		procedure.setRequired(reqDocs);

		FormField field2 = new FormField();

		field2.setType("text");
		field2.setSubtype("text");
		field2.setRequired(true);
		field2.setLabel("Direcci\u00F3n");
		field2.setDescription("Direcci\u00F3n de residencia");
		field2.setPlaceHolder("Calle 20 # 34 56");
		field2.setClassname("form-control");
		field2.setName("direccion");
		field2.setMaxlenght(100);

		formFields.add(field2);

		FormField field3 = new FormField();

		field3.setType("text");
		field3.setSubtype("text");
		field3.setRequired(true);
		field3.setLabel("Barrio");
		field3.setDescription("Barrio");
		field3.setPlaceHolder("Barrio");
		field3.setClassname("form-control");
		field3.setName("barrio");
		field3.setMaxlenght(50);

		formFields.add(field3);

		FormField field4 = new FormField();

		field4.setType("text");
		field4.setSubtype("tel");
		field4.setRequired(true);
		field4.setLabel("Tel\u00E9fono");
		field4.setDescription("N\u00FAmero telef\u00F3nico de contacto");
		field4.setPlaceHolder("3-----");
		field4.setClassname("form-control");
		field4.setName("telefono");
		field4.setMaxlenght(10);

		formFields.add(field4);

		FormField field5 = new FormField();

		field5.setType("textarea");
		field5.setRequired(true);
		field5.setLabel("Carta de solicitud");
		field5.setDescription("Carta de solicitud");
		field5.setPlaceHolder("Por favor diligencie su petici\u00F3n detalladamente");
		field5.setClassname("form-control");
		field5.setName("carta");
		field5.setMaxlenght(5000);

		formFields.add(field5);

		procedure.setFields(formFields);

		logger.info("inserting new procedure instance");

		System.out.println(procedure.getFields());
		collection.insertOne(procedure.toDocument());

	}

	// Procedure2
	public static void addProcedureDos(String pCode, String pName, String pMayoralty, String pFunctionary) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDURE_COLLECTION);

		ArrayList<FormField> formFields = new ArrayList<FormField>();
		ArrayList<RequiredUpload> reqDocs = new ArrayList<RequiredUpload>();

		Procedure procedure = new Procedure();
		procedure.setCode(pCode);
		procedure.setName(pName);
		procedure.setMayoralty(pMayoralty);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Atenci\u00F3n al ciudadano");
		activity1.setFunctionary(pFunctionary);
		activity1.setStatus(Constants.STATUS_PENDING);

		activities.add(activity1);

		procedure.setActivities(activities);

		// Required
		RequiredUpload reqDoc1 = new RequiredUpload();

		reqDoc1.setType("file");
		reqDoc1.setRequired(true);
		reqDoc1.setClassName("form-control");

		reqDoc1.setLabel("C\u00E9dula de Ciudadan\u00EDa");
		reqDoc1.setDescription("Adjunte su c\u00E9dula en formato (png, jpeg)");
		reqDoc1.setName("cedulaAtt");

		reqDocs.add(reqDoc1);

		RequiredUpload reqDoc2 = new RequiredUpload();

		reqDoc2.setType("file");
		reqDoc2.setRequired(true);
		reqDoc2.setClassName("form-control");

		reqDoc2.setLabel("Certificado Sisben");
		reqDoc2.setDescription("Adjunte su recibo en formato (png, jpeg)");
		reqDoc2.setName("sisbenAtt");

		reqDocs.add(reqDoc2);

		if (pName.equals("Anapoima")) {

			RequiredUpload reqDoc3 = new RequiredUpload();

			reqDoc3.setType("file");
			reqDoc3.setRequired(true);
			reqDoc3.setClassName("form-control");

			reqDoc3.setLabel("Certificado Presidente de la junta");
			reqDoc3.setDescription("Adjunte su recibo en formato (png, jpeg)");
			reqDoc3.setName("juntaAtt");

			reqDocs.add(reqDoc3);
		}

		procedure.setRequired(reqDocs);

		// Form

		// FormField field1 = new FormField();
		//
		// field1.setType("text");
		// field1.setSubtype("tel");
		// field1.setRequired(true);
		// field1.setLabel("Identificaci\u00F3n");
		// field1.setDescription("N\u00FAmero de documento de identidad");
		// field1.setPlaceHolder("123456789");
		// field1.setClassname("form-control");
		// field1.setName("identification");
		// field1.setMaxlenght(11);
		//
		// formFields.add(field1);

		FormField field2 = new FormField();

		field2.setType("text");
		field2.setSubtype("text");
		field2.setRequired(true);
		field2.setLabel("Direcci\u00F3n");
		field2.setDescription("Direcci\u00F3n de residencia");
		field2.setPlaceHolder("Calle 20 # 34 56");
		field2.setClassname("form-control");
		field2.setName("direccion");
		field2.setMaxlenght(100);

		formFields.add(field2);

		FormField field3 = new FormField();

		field3.setType("text");
		field3.setSubtype("text");
		field3.setRequired(true);
		field3.setLabel("Barrio");
		field3.setDescription("Barrio");
		field3.setPlaceHolder("Barrio");
		field3.setClassname("form-control");
		field3.setName("barrio");
		field3.setMaxlenght(50);

		formFields.add(field3);

		FormField field4 = new FormField();

		field4.setType("text");
		field4.setSubtype("tel");
		field4.setRequired(true);
		field4.setLabel("Tel\u00E9fono");
		field4.setDescription("N\u00FAmero telef\u00F3nico de contacto");
		field4.setPlaceHolder("3-----");
		field4.setClassname("form-control");
		field4.setName("telefono");
		field4.setMaxlenght(10);

		formFields.add(field4);

		FormField field5 = new FormField();

		field5.setType("textarea");
		field5.setRequired(true);
		field5.setLabel("Carta de solicitud");
		field5.setDescription("Carta de solicitud");
		field5.setPlaceHolder("Por favor diligencie su petici\u00F3n detalladamente");
		field5.setClassname("form-control");
		field5.setName("carta");
		field5.setMaxlenght(5000);

		formFields.add(field5);

		procedure.setFields(formFields);

		logger.info("inserting new procedure instance");

		System.out.println(procedure.getFields());
		collection.insertOne(procedure.toDocument());

	}

	// Procedure3
	public static void addProcedureTres(String pCode, String pName, String pMayoralty, String pFunctionary) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDURE_COLLECTION);

		ArrayList<FormField> formFields = new ArrayList<FormField>();
		ArrayList<RequiredUpload> reqDocs = new ArrayList<RequiredUpload>();
		ArrayList<Activity> activities = new ArrayList<Activity>();

		Procedure procedure = new Procedure();
		procedure.setCode(pCode);
		procedure.setName(pName);
		procedure.setMayoralty(pMayoralty);

		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary(pFunctionary);
		activity1.setStatus(Constants.STATUS_PENDING);

		activities.add(activity1);

		procedure.setActivities(activities);

		// Required
		RequiredUpload reqDoc1 = new RequiredUpload();

		reqDoc1.setType("file");
		reqDoc1.setRequired(true);
		reqDoc1.setClassName("form-control");

		reqDoc1.setLabel("C\u00E9dula de ciudadan\u00EDa del solicitante");
		reqDoc1.setDescription("Adjunte su c\u00E9dula en formato (png, jpeg)");
		reqDoc1.setName("cedulaAtt");

		reqDocs.add(reqDoc1);

		RequiredUpload reqDoc2 = new RequiredUpload();

		reqDoc2.setType("file");
		reqDoc2.setRequired(true);
		reqDoc2.setClassName("form-control");

		reqDoc2.setLabel("Comprobante de Pago");
		reqDoc2.setDescription("Adjunte su comprobante (png, jpeg)");
		reqDoc2.setName("facturaAtt");

		reqDocs.add(reqDoc2);

		RequiredUpload reqDoc3 = new RequiredUpload();

		reqDoc3.setType("file");
		reqDoc3.setRequired(true);
		reqDoc3.setClassName("form-control");

		reqDoc3.setLabel("C\u00E9dula del fallecido");
		reqDoc3.setDescription("Adjunte la c\u00E9dula del fallecido (png, jpeg)");
		reqDoc3.setName("cedulaFallecidoAtt");

		reqDocs.add(reqDoc3);

		RequiredUpload reqDoc4 = new RequiredUpload();

		reqDoc4.setType("file");
		reqDoc4.setRequired(true);
		reqDoc4.setClassName("form-control");

		reqDoc4.setLabel("Certificado de defunci\u00F3n");
		reqDoc4.setDescription("Adjunte el certificado de defunci\u00F3n (png, jpeg)");
		reqDoc4.setName("defuncionAtt");

		reqDocs.add(reqDoc4);

		RequiredUpload reqDoc5 = new RequiredUpload();

		reqDoc5.setType("file");
		reqDoc5.setRequired(true);
		reqDoc5.setClassName("form-control");

		reqDoc5.setLabel("Certificado de cuenta bancaria");
		reqDoc5.setDescription("Adjunte el certificado de la cuenta bancaria (png, jpeg)");
		reqDoc5.setName("cuentaAtt");

		reqDocs.add(reqDoc5);

		procedure.setRequired(reqDocs);

		// Form
		FormField field2 = new FormField();

		field2.setType("text");
		field2.setSubtype("text");
		field2.setRequired(true);
		field2.setLabel("Direcci\u00F3n");
		field2.setDescription("Direcci\u00F3n de residencia");
		field2.setPlaceHolder("Calle 20 # 34 56");
		field2.setClassname("form-control");
		field2.setName("direccion");
		field2.setMaxlenght(100);

		formFields.add(field2);

		FormField field3 = new FormField();

		field3.setType("text");
		field3.setSubtype("text");
		field3.setRequired(true);
		field3.setLabel("Barrio");
		field3.setDescription("Barrio");
		field3.setPlaceHolder("Barrio");
		field3.setClassname("form-control");
		field3.setName("barrio");
		field3.setMaxlenght(50);

		formFields.add(field3);

		FormField field4 = new FormField();

		field4.setType("text");
		field4.setSubtype("tel");
		field4.setRequired(true);
		field4.setLabel("Tel\u00E9fono");
		field4.setDescription("N\u00FAmero telef\u00F3nico de contacto");
		field4.setPlaceHolder("3-----");
		field4.setClassname("form-control");
		field4.setName("telefono");
		field4.setMaxlenght(10);

		formFields.add(field4);

		FormField field5 = new FormField();

		field5.setType("textarea");
		field5.setRequired(true);
		field5.setLabel("Carta de solicitud");
		field5.setDescription("Carta de solicitud");
		field5.setPlaceHolder("Por favor diligencie su petici\u00F3n detalladamente");
		field5.setClassname("form-control");
		field5.setName("carta");
		field5.setMaxlenght(5000);

		formFields.add(field5);

		procedure.setFields(formFields);

		logger.info("inserting new procedure instance");

		System.out.println(procedure.getFields());
		collection.insertOne(procedure.toDocument());

	}

	/*
	 * Procedure: This is not an actual procedure from *Cundinamarca* (it is not
	 * even a procedure) Added to test several activities SCC
	 */
	public static void addProcedureCuatro(String pCode, String pName, String pMayoralty) {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDURE_COLLECTION);

		ArrayList<FormField> formFields = new ArrayList<FormField>();
		ArrayList<RequiredUpload> reqDocs = new ArrayList<RequiredUpload>();
		ArrayList<Activity> activities = new ArrayList<Activity>();

		Procedure procedure = new Procedure();
		procedure.setCode(pCode);
		procedure.setName(pName);
		procedure.setMayoralty(pMayoralty);

		// Activities
		activities.add(new Activity("CDP", "Certificado de disponibilidad Presupuestal", "Jefe de presupuesto", 1,
				"acalle@elrosal.gov.co", Constants.STATUS_PENDING));
		activities.add(new Activity("Elaboraci\u00F3n Contrato", "Realizaci\u00F3n del contrato",
				"Coordinador de deportes ", 2, "acalle@elrosal.gov.co", Constants.STATUS_PENDING));
		activities.add(new Activity("Aprobaci\u00F3n juridica", "Aprobaci\u00F3n de propuesta", "Juridico", 3,
				"acalle@elrosal.gov.co", Constants.STATUS_PENDING));
		activities.add(new Activity("Firma Alcalde", "Firma del alcalde y Documento de supervisor del contrato",
				"Alcalde", 4, "acalle@elrosal.gov.co", Constants.STATUS_PENDING));
		activities.add(new Activity("Firma Ciudadano", "Firma del contrato por el ciudadano", "Ciudadan\u00EDa", 5, "",
				Constants.STATUS_PENDING));
		activities.add(new Activity("RP", "Responsabilidad presupuestal", "Jefe de presupuesto", 6,
				"jvaldez@elrosal.gov.co", Constants.STATUS_PENDING));
		activities.add(new Activity("Informe de labor", "Informe del objeto contractual", "Ciudadan\u00EDa", 7, "",
				Constants.STATUS_PENDING));
		activities.add(new Activity("Informe supervisi\u00F3n", "Informe de supervisi\u00F3n del contrato",
				"Coordinador del contrato", 8, "acalle@elrosal.gov.co", Constants.STATUS_PENDING));
		activities.add(new Activity("Orden de pago", "Solicitud de orden de pago y comprobante de egreso", "Tesoreria",
				9, "acalle@elrosal.gov.co", Constants.STATUS_PENDING));

		procedure.setActivities(activities);

		// Required

		reqDocs.add(new RequiredUpload("file", true, "C\u00E9dula de Ciudadan\u00EDa del solicitante",
				"Adjunte su c\u00E9dula en formato (png, jpeg)", "form-control", "cedulaAtt"));
		reqDocs.add(new RequiredUpload("file", true, "Documento de proyecto", "Adjunte el documento de proyecto (pdf)",
				"form-control", "cedulaAtt"));
		reqDocs.add(new RequiredUpload("file", true, "Recibo seguridad social",
				"Adjunte el recibo seguridad social (png, jpeg)", "form-control", "cedulaAtt"));
		reqDocs.add(new RequiredUpload("file", true, "Hoja de vida de funci\u00F3n publica",
				"Adjunte su hoja de vida de funci\u00F3n publica (pdf)", "form-control", "cedulaAtt"));
		reqDocs.add(new RequiredUpload("file", true, "Antecedentes Contraloria",
				"Adjunte sus antecedentes contraloria (pdf, png, jpeg)", "form-control", "cedulaAtt"));
		reqDocs.add(new RequiredUpload("file", true, "Antecedentes Fiscales",
				"Adjunte sus antecedentes fiscales(pdf, png, jpeg)", "form-control", "cedulaAtt"));
		reqDocs.add(new RequiredUpload("file", true, "RUT", "Adjunte su rut (pdf, png, jpeg)", "form-control",
				"cedulaAtt"));
		procedure.setRequired(reqDocs);

		// Form
		FormField field2 = new FormField();

		field2.setType("text");
		field2.setSubtype("text");
		field2.setRequired(true);
		field2.setLabel("Direcci\u00F3n");
		field2.setDescription("Direcci\u00F3n de residencia");
		field2.setPlaceHolder("Calle 20 # 34 56");
		field2.setClassname("form-control");
		field2.setName("direccion");
		field2.setMaxlenght(100);

		formFields.add(field2);

		FormField field3 = new FormField();

		field3.setType("text");
		field3.setSubtype("text");
		field3.setRequired(true);
		field3.setLabel("Barrio");
		field3.setDescription("Barrio");
		field3.setPlaceHolder("Barrio");
		field3.setClassname("form-control");
		field3.setName("barrio");
		field3.setMaxlenght(50);

		formFields.add(field3);

		FormField field4 = new FormField();

		field4.setType("text");
		field4.setSubtype("tel");
		field4.setRequired(true);
		field4.setLabel("Tel\u00E9fono");
		field4.setDescription("N\u00FAmero telef\u00F3nico de contacto");
		field4.setPlaceHolder("3-----");
		field4.setClassname("form-control");
		field4.setName("telefono");
		field4.setMaxlenght(10);

		formFields.add(field4);

		FormField field5 = new FormField();

		field5.setType("textarea");
		field5.setRequired(true);
		field5.setLabel("Carta de solicitud");
		field5.setDescription("Carta de solicitud");
		field5.setPlaceHolder("Por favor diligencie su petici\u00F3n detalladamente");
		field5.setClassname("form-control");
		field5.setName("carta");
		field5.setMaxlenght(5000);

		formFields.add(field5);

		procedure.setFields(formFields);

		logger.info("inserting new procedure instance");

		System.out.println(procedure.getFields());
		collection.insertOne(procedure.toDocument());

	}

	// ProcedureRequest1
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestUno() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Certificado de Residencia");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("aosorio@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Andres");
		citizen.setLastName1("Osorio");
		citizen.setLastName2("Vargas");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("Anapoima");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 1 # 12 21");
		procedureData.put("Barrio", "El Castillo");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary("anapoima");
		activity1.setAprobacion("En proceso");
		activity1.setStatus(Constants.STATUS_PENDING);

		activities.add(activity1);
		procedureRequest.setActivities(activities);
		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);

		procedureRequest.setStartDate(new Date("2016/07/14"));
		procedureRequest.setFinishDate(null);
		procedureRequest.setStatus("En proceso");

		logger.info("inserting new procedure request instance");
		try {

			collection.insertOne(procedureRequest.toDocument());
		} catch (Exception e) {
			logger.info("addProcedureRequestUno " + e.getMessage());
		}

	}

	// ProcedureRequest2
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestDos() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Certificado de Residencia");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("aosorio@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Andres");
		citizen.setLastName1("Osorio");
		citizen.setLastName2("Vargas");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("Anapoima");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 2 # 23 45");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary("acalle@anapoima.gov.co");
		activity1.setAprobacion("Finalizado");
		activity1.setStatus(Constants.STATUS_PENDING);

		activities.add(activity1);
		procedureRequest.setActivities(activities);

		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);

		procedureRequest.setStartDate(new Date("2016/07/14"));
		procedureRequest.setFinishDate(new Date("2016/08/14"));
		procedureRequest.setStatus("Finalizado");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest3
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestTres() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Auxilio para Gastos Sepelio");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("f.hernandez@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Fabian");
		citizen.setLastName1("Hernandez");
		citizen.setLastName2("Schmidt");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("El Rosal");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 2 # 23 45");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");
		deliveryDocs.put("Doc3", "estaEsLARutaAlDoc3");

		procedureRequest.setDeliveryDocs(deliveryDocs);
		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary("acalle@elrosal.gov.co");
		activity1.setAprobacion("Finalizado");
		activity1.setStatus(Constants.STATUS_PENDING);

		activities.add(activity1);
		procedureRequest.setActivities(activities);

		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);

		procedureRequest.setStartDate(new Date("2016/07/21"));
		procedureRequest.setFinishDate(new Date("2016/09/21"));
		procedureRequest.setStatus("Finalizado");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest4
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestCuatro() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Auxilio para Gastos Sepelio");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("f.hernandez@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Fabian");
		citizen.setLastName1("Hernandez");
		citizen.setLastName2("Schmidt");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("El Rosal");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 10 # 10 30");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");
		deliveryDocs.put("Doc3", "estaEsLARutaAlDoc3");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Atenci\u00F3n al ciudadano");
		activity1.setFunctionary("acalle@elrosal.gov.co");
		activity1.setAprobacion("En proceso");
		activity1.setStatus("En curso");

		activities.add(activity1);
		procedureRequest.setActivities(activities);

		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);

		procedureRequest.setStartDate(new Date("2016/08/06"));
		procedureRequest.setFinishDate(null);
		procedureRequest.setStatus("En proceso");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest5
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestCinco() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Certificado de Residencia");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("jl.rodriguez@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Jheison");
		citizen.setLastName1("Rodriguez");
		citizen.setLastName2("Borja");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("Anapoima");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 1 # 12 21");
		procedureData.put("Barrio", "El Castillo");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary("jvaldez@anapoima.gov.co");
		activity1.setAprobacion("En proceso");
		activity1.setStatus("En curso");

		activities.add(activity1);
		procedureRequest.setActivities(activities);
		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);
		procedureRequest.setStartDate(new Date("2016/07/14"));
		procedureRequest.setFinishDate(null);
		procedureRequest.setStatus("En proceso");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest6
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestSeis() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Certificado de Residencia");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("jl.rodriguez@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Jheison");
		citizen.setLastName1("Rodriguez");
		citizen.setLastName2("Borja");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("Anapoima");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 2 # 23 45");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary("acalle@anapoima.gov.co");
		activity1.setAprobacion("Finalizado");
		activity1.setStatus("En curso");

		activities.add(activity1);
		procedureRequest.setActivities(activities);
		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);
		procedureRequest.setStartDate(new Date("2016/07/14"));
		procedureRequest.setFinishDate(new Date("2016/08/14"));
		procedureRequest.setStatus("Finalizado");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest7
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestSiete() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Auxilio para Gastos Sepelio");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("df.martinez1@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("David");
		citizen.setLastName1("Martinez");
		citizen.setLastName2("Salcedo");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("El Rosal");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 2 # 23 45");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");
		deliveryDocs.put("Doc3", "estaEsLARutaAlDoc3");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Hacienda");
		activity1.setFunctionary("jvaldez@elrosal.gov.co");
		activity1.setAprobacion("Finalizado");
		activity1.setStatus("En curso");

		activities.add(activity1);
		procedureRequest.setActivities(activities);

		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);
		procedureRequest.setStartDate(new Date("2016/07/21"));
		procedureRequest.setFinishDate(new Date("2016/09/21"));
		procedureRequest.setStatus("Finalizado");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest8
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestOcho() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Auxilio para Gastos Sepelio");
		try {

			ExternalSvcInvoker.invoke(Routes.BARCODER_EXTSVC_ROUTE);
			JsonObject json = (JsonObject) ExternalSvcInvoker.getResponse();
			procedureRequest.setFileNumber(json.get("code").getAsString());

		} catch (FileNotFoundException | UnknownHostException ex) {
			logger.info("Problem reaching external service");
			procedureRequest.setFileNumber(UUID.randomUUID().toString());
		}

		Citizen citizen = new Citizen();
		citizen.setEmail("df.martinez1@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("David");
		citizen.setLastName1("Martinez");
		citizen.setLastName2("Salcedo");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("El Rosal");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 10 # 10 30");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");
		deliveryDocs.put("Doc3", "estaEsLARutaAlDoc3");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Atenci\u00F3n al ciudadano");
		activity1.setFunctionary("acalle@anapoima.gov.co");
		activity1.setAprobacion("En proceso");
		activity1.setStatus("En curso");

		activities.add(activity1);
		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);
		procedureRequest.setActivities(activities);
		procedureRequest.setStartDate(new Date("2016/08/06"));
		procedureRequest.setFinishDate(null);
		procedureRequest.setStatus("En proceso");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	// ProcedureRequest9
	// SCC
	@SuppressWarnings("deprecation")
	public static <V> void addProcedureRequestNueve() {

		MongoDatabase dbOne = DatabaseSingleton.getInstance().getDatabase();
		MongoCollection<Document> collection = dbOne.getCollection(Constants.PROCEDUREREQUEST_COLLECTION);

		ProcedureRequest procedureRequest = new ProcedureRequest();

		procedureRequest.setProcedureClassName("Solicitud De Contratacion Monitor Deportes");
		procedureRequest.setFileNumber("9");

		Citizen citizen = new Citizen();
		citizen.setEmail("s.cardona12@uniandes.edu.co");
		citizen.setIdentification(123456);
		citizen.setName("Sebastian");
		citizen.setLastName1("Cardona");
		citizen.setLastName2("Correa");
		citizen.setBirthDate(getBirthdate());

		procedureRequest.setCitizen(citizen);
		procedureRequest.setMayoralty("El Rosal");

		Document procedureData = new Document();
		procedureData.put("Identificaci\u00F3n", 123456);
		procedureData.put("Direcci\u00F3n", "Calle 10 # 10 30");
		procedureData.put("Barrio", "La Soledad");
		procedureData.put("Tel\u00E9fono", 55667733);
		procedureData.put("Carta de solicitud", "Solicito amablemente un certificado de residencia");

		procedureRequest.setProcedureData(procedureData);

		Document deliveryDocs = new Document();
		deliveryDocs.put("Doc1", "estaEsLARutaAlDoc1");
		deliveryDocs.put("Doc2", "estaEsLARutaAlDoc2");
		deliveryDocs.put("Doc3", "estaEsLARutaAlDoc3");

		procedureRequest.setDeliveryDocs(deliveryDocs);

		ArrayList<Activity> activities = new ArrayList<Activity>();
		// Activities
		Activity activity1 = new Activity();
		activity1.setStep(1);
		activity1.setName("Aprobaci\u00F3n");
		activity1.setDescription("Revisar documentaci\u00F3n y aprobar");
		activity1.setDependency("Atenci\u00F3n al ciudadano");
		activity1.setFunctionary("acalle@anapoima.gov.co");
		activity1.setAprobacion("En proceso");
		activity1.setStatus("En curso");

		activities.add(activity1);
		// History
		ArrayList<History> histories = new ArrayList<History>();
		histories.add(new History(0, "2016/10/26", citizen.getEmail(), "Iniciar", "Se inicia tramite"));
		procedureRequest.setHistories(histories);
		procedureRequest.setActivities(activities);
		procedureRequest.setStartDate(new Date("2016/08/06"));
		procedureRequest.setFinishDate(null);
		procedureRequest.setStatus("En proceso");

		logger.info("inserting new procedure request instance");

		collection.insertOne(procedureRequest.toDocument());

	}

	public static void addProcedureRequest() {

		ProcedureRequest procedure = new ProcedureRequest();

		procedure.setProcedureClassName("Certificado de residencia");

		Mayoralty mayorality = new Mayoralty();
		mayorality.setName("Anapoima");
		mayorality.setAddress("CRA 123 45 6");
		mayorality.setUrl("https://anapoima.gov.co");
		mayorality.setPhone("333555888");

		Citizen citizen = new Citizen();
		citizen.setName("Juan");
		citizen.setLastName1("Valdes");
		citizen.setIdentification(1234567890);
		citizen.setEmail("jvaldes@uniandes.edu.co");
		citizen.setPassword("Qwerty");
		citizen.setUserProfile(Constants.CITIZEN_COLLECTION);
		citizen.setBirthDate(getBirthdate());

	}

	/**
	 * get jetty server full URL
	 * 
	 * @return
	 */
	public static String getServerPath() {

		int port = App.JETTY_SERVER_PORT;
		String server = "http://localhost";
		logger.info("JETTY SERVER PORT: " + port);
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(server);
		strBuilder.append(":");
		strBuilder.append(port);
		String serverPath = strBuilder.toString();

		return serverPath;
	}

	/**
	 * Remove a created file from local storage
	 * 
	 * @param pFileName
	 */
	public static void removeTestFile(String pFileName) {

		File file = new File(pFileName);

		try {
			Files.delete(file.toPath());
			logger.info("Temporay file deleted: " + file.getAbsolutePath());
		} catch (NoSuchFileException x) {
			logger.error("%s: no such" + " file or directory%n", file);
		} catch (DirectoryNotEmptyException x) {
			logger.error("%s not empty%n", file);
		} catch (IOException x) {
			// File permission problems are caught here.
			logger.info("removeTestFile> failure");
			logger.error(x.getMessage());
		}
	}

	/**
	 * create a temporary file for this test
	 * 
	 * @param pFileName
	 */
	public static void createTestFile(String pFileName) {

		BufferedWriter writer = null;
		try {

			File logFile = new File(pFileName);
			String filePath = logFile.getCanonicalPath();
			logger.info(filePath);

			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write("Hello world!");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @throws Exception
	 */
	public static String getTmpDir() throws Exception {

		String tmpPath = null;
		ArrayList<String> localStorage = new ArrayList<String>();

		localStorage.add("LOCAL_TMP_PATH_ENV"); // This is the preferred
												// environment variable
		localStorage.add("TMP"); // second best - linux, windows
		localStorage.add("HOME"); // if previous fail, last chance

		Iterator<String> itrPath = localStorage.iterator();

		boolean found = false;

		// Get the TMP_PATH from an environment variable
		while (itrPath.hasNext()) {
			String testPath = itrPath.next();
			String value = System.getenv(testPath);
			if (value != null) {
				tmpPath = value;
				found = true;
				/*
				 * File directoryName = new File(value + "/junittest"); if
				 * (!directoryName.exists()) { logger.info(
				 * "creating directory: " + directoryName); boolean result =
				 * false; try { directoryName.mkdir(); result = true; } catch
				 * (SecurityException se) {
				 * System.out.println(se.getLocalizedMessage()); } if (result) {
				 * tmpPath = directoryName.toString(); logger.info(
				 * "LOCALTMP + /junittest created"); } } else { tmpPath =
				 * directoryName.toString(); }
				 */
				break;
			}
		}
		if (!found)
			throw new Exception("TMP not defined!");

		return tmpPath;

	}

	/**
	 * Check if the input directory exists, if not then create it
	 * 
	 * @param inputDir
	 */
	public static void checkDir(String inputDir) {

		File theDir = new File(inputDir);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("creating directory: " + inputDir);
			boolean result = false;

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if (result) {
				logger.info("DIR created");
			}
		}
	}

	public static void isConnected() throws IOException {

		String strUrl = "http://stackoverflow.com/about";

		try {
			URL url = new URL(strUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.connect();

		} catch (IOException e) {
			logger.error("Error creating HTTP connection");
			e.printStackTrace();
			throw e;
		}

	}

	public static Date getBirthdate() {

		Date birthDate = null;

		try {
			birthDate = dateFormat.parse(birthDateStr);
		} catch (ParseException e) {
			logger.error("Bad date format");
			e.printStackTrace();
		}

		return birthDate;
	}

}
