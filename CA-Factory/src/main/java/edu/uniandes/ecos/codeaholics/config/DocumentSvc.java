/** Copyright or License
 *
 */

package edu.uniandes.ecos.codeaholics.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.uniandes.ecos.codeaholics.persistence.RequiredDocument;
import spark.Request;
import spark.Response;

/**
 * Package: edu.uniandes.ecos.codeaholics.config
 *
 * Class: DocumentSvc DocumentSvc.java
 * 
 * Original Author: @author AOSORIO
 * 
 * Description: Class implementing the document/file service
 * 
 * Implementation: Implements IDocumentSvc
 *
 * Created: Aug 20, 2016 10:27:12 AM
 * 
 */
public class DocumentSvc implements IDocumentSvc {

	Logger logger = LogManager.getRootLogger();

	private String answerStr = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uniandes.ecos.codeaholics.config.IDocumentSvc#uploadDocument(Request
	 * pRequest)
	 */
	@Override
	public void uploadDocument(Request pRequest) {

		File uploadDir = null;
		
		//TODO: this is a public route. We go with this solution in the meantime
		String sparkDocDirectory = new File(".").getAbsolutePath().toString() + "/src/main/resources/public/docs/";
		long timestamp = System.currentTimeMillis();
		
		try {
			FileUtil.configTmpDir(); //capture LOCAL_TMP_PATH from environment
			//uploadDir = new File(FileUtil.LOCAL_TMP_PATH);
			//logger.info("LOCAL_TMP_PATH=" + FileUtil.LOCAL_TMP_PATH);
			uploadDir = new File(sparkDocDirectory);
			uploadDir.mkdir();

		} catch (Exception e) {
		
			uploadDir = new File(FileUtil.LOCAL_TMP_PATH);
			uploadDir.mkdir();
			logger.error(e.getMessage());
		}

		pRequest.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try {

			Collection<Part> parts;
			parts = pRequest.raw().getParts();
			Part fPart = parts.iterator().next();

			InputStream input = fPart.getInputStream();

			String fileExt = null;

			try {
				logger.info(fPart.getSubmittedFileName());
				fileExt = fPart.getSubmittedFileName().split(".")[1];
			} catch (Exception ioex) {
				fileExt = ".tmp";
			}

			Path tempFile = Files.createTempFile(uploadDir.toPath(), String.valueOf(timestamp), fileExt);

			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);

			RequiredDocument rDoc;

			rDoc = new RequiredDocument();
			rDoc.setFilePath(sparkDocDirectory);
			rDoc.setTmpName(tempFile.getFileName().toString());
			rDoc.setOriginalName(fPart.getSubmittedFileName());
			rDoc.setFileSize(fPart.getSize());
			rDoc.setRadicado(123456);
			rDoc.setTimestamp(timestamp);

			Gson gson = new Gson();
			answerStr = gson.toJson(rDoc);
			logger.info(answerStr);

		} catch (IOException | ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uniandes.ecos.codeaholics.config.IDocumentSvc#downloadDocument()
	 */
	@Override
	public HttpServletResponse downloadDocument(Request pRequest, Response pResponse) {

		String locationDir = null;
		Path path = null;
		String outFileName = null;
		try {
			FileUtil.configTmpDir();
			locationDir = FileUtil.LOCAL_TMP_PATH;
			String key = pRequest.queryParams("filepath");
			path = Paths.get(locationDir + "/" + key);
			outFileName = key;

		} catch (Exception e) {
			locationDir = "";
			logger.error(e.getMessage());
		}

		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (Exception e1) {

			e1.printStackTrace();
		}

		HttpServletResponse raw = pResponse.raw();
		pResponse.header("Content-Disposition", "attachment; filename=" + outFileName);
		pResponse.type("application/force-download");
		try {
			raw.getOutputStream().write(data);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return raw;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uniandes.ecos.codeaholics.config.IDocumentSvc#downloadDocument()
	 */
	public HttpServletResponse downloadDocument(String locationDir, String name, HttpServletResponse raw) {
		try {
			Path path = Paths.get(locationDir + "/" + name);
			byte[] data = null;
			data = Files.readAllBytes(path);

			raw.getOutputStream().write(data);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();

			return raw;
		} catch (Exception e) {

			e.printStackTrace();
			locationDir = "";
			logger.error(e.getMessage());
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uniandes.ecos.codeaholics.config.IDocumentSvc#listDocuments()
	 */
	@Override
	public String listDocuments() {

		String allFiles = "";

		try {

			FileUtil.configTmpDir();
			ArrayList<String> files = FileUtil.listDownloadedFiles(FileUtil.LOCAL_TMP_PATH);
			Iterator<String> itrFiles = files.iterator();
			allFiles += "[";

			while (itrFiles.hasNext()) {
				String jsonStr = "{\"file" + "\": \"" + itrFiles.next().replace("\\", "/") + "\"}";
				JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(jsonStr).getAsJsonObject();
				allFiles += obj.toString() + ",";
			}
			allFiles = FileUtil.removeLastChar(allFiles) + "]";

		} catch (Exception e) {
			allFiles = "[{}]";
			e.printStackTrace();
		}

		return allFiles;

	}

	/**
	 * @return the answerStr
	 */
	public String getAnswerStr() {
		return answerStr;
	}

}
