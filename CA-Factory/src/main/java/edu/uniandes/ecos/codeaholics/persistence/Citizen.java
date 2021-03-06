/** Copyright or License
 *
 */

package edu.uniandes.ecos.codeaholics.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

import com.google.gson.annotations.SerializedName;

/**
 * Created by davidMtz22 on 18/07/2016.
 */
public class Citizen {

	public static final String IDENTIFICATION = "identification";
	public static final String NAME = "name";
	public static final String LASTNAME1 = "lastName1";
	public static final String LASTNAME2 = "lastName2";
	public static final String EMAIL = "email";
	public static final String PASSWORD = "password";
	public static final String SALT = "salt";
	public static final String BIRTHDATE = "birthDate";
	public static final String PROFILE = "userProfile";

	public static final String IDENTIFICATION_ES = "Identificaci\u00F3n";
	public static final String NAME_ES = "Nombre";
	public static final String LASTNAME1_ES = "Primer Apellido";
	public static final String LASTNAME2_ES = "Segundo Apellido";

	@SerializedName("_id")
	protected String _id;
	private Integer identification;
	private String name;
	private String lastName1;
	private String lastName2;
	private String email;
	private String password;
	private String salt;
	private Date birthDate;
	private String userProfile;

	/**
	 * @return the userProfile
	 */
	public String getUserProfile() {
		return userProfile;
	}

	/**
	 * @param userProfile
	 *            the userProfile to set
	 */
	public void setUserProfile(String userProfile) {
		this.userProfile = userProfile;
	}

	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public Integer getIdentification() {
		return identification;
	}

	public void setIdentification(Integer identification) {
		this.identification = identification;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName1() {
		return lastName1;
	}

	public void setLastName1(String lastName1) {
		this.lastName1 = lastName1;
	}

	public String getLastName2() {
		return lastName2;
	}

	public void setLastName2(String lastName2) {
		this.lastName2 = lastName2;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Date getBirthDate() {
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		// formatter.applyPattern(String.valueOf(birthDate));
		// System.out.println(birthDate);
		// try {
		// this.birthDate = formatter.parse(String.valueOf(birthDate));
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public void setBirthDate(String birthDateStr) {
		
		System.out.println(birthDate);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddThh:mm:ssZ");		

		try {
			this.birthDate = dateFormat.parse(birthDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Default constructor - no arguments
	 */
	public Citizen() {
		
	}
	
	/**
	 * Copy Constructor - just in case if needed - AO
	 * @param otherCitizen: citizen we want to make a copy into this
	 */
	public Citizen(Citizen otherCitizen) {
		
		this.identification = otherCitizen.getIdentification();
		this.name = otherCitizen.getName();
		this.lastName1 = otherCitizen.getLastName1();
		this.lastName2 = otherCitizen.getLastName2();
		this.email = otherCitizen.getEmail();
		this.password = otherCitizen.getPassword();
		this.salt = otherCitizen.getSalt();
		this.birthDate = otherCitizen.getBirthDate();
		this.userProfile = otherCitizen.getUserProfile();
	
	}


	public Document toDocument() {
		Document citizen = new Document();
		citizen.append(IDENTIFICATION, this.getIdentification()).append(NAME, this.getName())
				.append(LASTNAME1, this.getLastName1()).append(LASTNAME2, this.getLastName2())
				.append(EMAIL, this.getEmail()).append(SALT, this.getSalt()).append(PASSWORD, this.getPassword())
				.append(BIRTHDATE, this.getBirthDate()).append(PROFILE, this.getUserProfile());

		return citizen;
	}

	public Document toDocumentES() {
		Document citizen = new Document();
		citizen.append(IDENTIFICATION_ES, this.getIdentification())
				.append(NAME_ES, this.getName() + " " + this.getLastName1() + " " + this.getLastName2())
				.append(EMAIL, this.getEmail());

		return citizen;
	}

}
