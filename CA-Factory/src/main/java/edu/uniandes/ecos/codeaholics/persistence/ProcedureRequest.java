/** Copyright or License
 *
 */

package edu.uniandes.ecos.codeaholics.persistence;

import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;

import com.google.gson.annotations.SerializedName;

/**
 * Created by davidMtz22 on 18/07/2016.
 */
public class ProcedureRequest {

	public static final String CLASSNAME = "className";
	public static final String FILENUMBER = "fileNumber";
	public static final String CITIZEN = "citizen";
	public static final String MAYORALTY = "mayoralty";
	public static final String PROCEDUREDATA = "procedureData";
	public static final String DELIVERYDOCS = "deliveryDocs";
	public static final String ACTIVITIES = "activities";
	public static final String STARTDATE = "startDate";
	public static final String FINISHDATE = "finishDate";
	public static final String STATUS = "status";
	public static final String HISTORIES = "histories"; // TODO: this is not a
														// correct name, for
														// example
														// change to
														// historicalRecords

	@SerializedName("_id")
	private String _id;
	private String className;
	private String fileNumber;
	private Citizen citizen;
	private String mayoralty;
	private Document procedureData;
	private Document deliveryDocs;
	private Date startDate;
	private Date finishDate;
	private String status;
	private ArrayList<Activity> activities;
	private ArrayList<History> histories;

	/*
	 * Definition of getters and setters
	 */
	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public String getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(String fileNumber) {
		this.fileNumber = fileNumber;
	}

	public Citizen getCitizen() {
		return citizen;
	}

	public void setCitizen(Citizen citizen) {
		this.citizen = citizen;
	}

	public String getMayoralty() {
		return mayoralty;
	}

	public void setMayoralty(String mayoralty) {
		this.mayoralty = mayoralty;
	}

	public Document getProcedureData() {
		return procedureData;
	}

	public void setProcedureData(Document procedureData) {
		this.procedureData = procedureData;
	}

	public Document getDeliveryDocs() {
		return deliveryDocs;
	}

	public void setDeliveryDocs(Document deliveryDocs) {
		this.deliveryDocs = deliveryDocs;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ArrayList<Activity> getActivities() {
		return activities;
	}

	public void setActivities(ArrayList<Activity> activities) {
		this.activities = activities;
	}

	public String getProcedureClassName() {
		return this.className;
	}

	public void setProcedureClassName(String procedureClassName) {
		this.className = procedureClassName;
	}
	
	public ArrayList<History> getHistories() {
		return histories;
	}

	public void setHistories(ArrayList<History> histories) {
		this.histories = histories;
	}


	/*
	 * 
	 * end of getters and setters definitions
	 * 
	 */

	public ArrayList<Document> activitiesDocuments() {
		ArrayList<Document> activitiesDocs = new ArrayList<Document>();
		if (!this.getActivities().isEmpty()) {
			for (int i = 0; i < this.getActivities().size(); i++) {
				activitiesDocs.add(this.getActivities().get(i).toDocument());
			}
		}
		return activitiesDocs;
	}

	/**
	 * @param history
	 */
	public void addHistory(History history) {
		if (this.histories == null) {
			this.histories = new ArrayList<History>();
			this.histories.add(history);
		} else {
			this.histories.add(history);
		}
	}

	/**
	 * @return
	 */
	public ArrayList<Document> historiesDocuments() {
		ArrayList<Document> historiesDoc = new ArrayList<Document>();
		if (!this.histories.isEmpty()) {
			for (int i = 0; i < this.histories.size(); i++) {
				historiesDoc.add(this.histories.get(i).toDocument());
			}
		}
		return historiesDoc;
	}

	/**
	 * @return
	 */
	public Document toDocument() {
		Document procedureRequest = new Document();
		procedureRequest.append(CLASSNAME, this.getProcedureClassName());
		procedureRequest.append(FILENUMBER, this.getFileNumber());
		procedureRequest.append(CITIZEN, this.getCitizen().toDocumentES()); // Citizen
																			// Att
																			// in
																			// ES
		procedureRequest.append(MAYORALTY, this.getMayoralty());
		procedureRequest.append(PROCEDUREDATA, this.getProcedureData());
		procedureRequest.append(DELIVERYDOCS, this.getDeliveryDocs());
		procedureRequest.append(ACTIVITIES, activitiesDocuments());
		procedureRequest.append(STARTDATE, this.getStartDate());
		procedureRequest.append(FINISHDATE, this.getFinishDate());
		procedureRequest.append(STATUS, this.getStatus());

		if (this.histories != null) {
			procedureRequest.append(HISTORIES, historiesDocuments());
		} else {
			procedureRequest.append(HISTORIES, null);
		}

		return procedureRequest;
	}

}
