package com.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.demo.data.FileUploadRepository;
import com.demo.model.FileUploadMetaData;

/** A rest controller provides api to upload single/multiple files as post request and get the uploaded files as an get request*/
@RestController
public class RestUploadController {
	private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);

	/** Save the uploaded file to this folder */
	private static String UPLOADED_FOLDER = "C:/work/spring-boot-work/upload/";

	/**
	 * This object is required to store file meta data into in memory database
	 */
	@Autowired
	private FileUploadRepository fileUploadMetaData;

	/**
	 * Single file upload
	 * @param uploadfile
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/api/fileupload", method = RequestMethod.POST)
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile,
			final HttpServletRequest request) {

		/** Below data is what we saving into database */
		logger.debug("Single file upload!");
		logger.debug("fileName : " + uploadfile.getOriginalFilename());
		logger.debug("contentType : " + uploadfile.getContentType());
		logger.debug("contentSize : " + uploadfile.getSize());

		if (uploadfile.isEmpty()) {
			return new ResponseEntity<String>("please select a file!", HttpStatus.OK);
		}

		try {
			/** File will get saved to file system and database */
			saveUploadedFiles(Arrays.asList(uploadfile));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>("Successfully uploaded - " + uploadfile.getOriginalFilename(),
				new HttpHeaders(), HttpStatus.OK);

	}

	/**
	 * Multiple files to upload
	 * @param extraField
	 * @param uploadfiles
	 * @return
	 */
	@RequestMapping(value = "/api/upload/multiplefiles", method = RequestMethod.POST)
	public ResponseEntity<?> uploadFileMulti(@RequestParam("files") MultipartFile[] uploadfiles) {
		logger.debug("Multiple file upload!");
		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));
		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity<String>("please select files!", HttpStatus.OK);
		}
		
		if (uploadfiles.length==0) {
			return new ResponseEntity<String>("please select files!", HttpStatus.OK);
		}
		
		try {
			/** File will get saved to file system and database */
			saveUploadedFiles(Arrays.asList(uploadfiles));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>("Successfully uploaded - " + uploadedFileName, HttpStatus.OK);

	}

	/**
	 * Rest endpoint api to get uploaded files 
	 * @return
	 */
	@RequestMapping(value = "/getFileUploadMetaData", method = RequestMethod.GET)
	public List<FileUploadMetaData> fileUploadMetaData() {
		List<FileUploadMetaData> fileMetaData = fileUploadMetaData.findAll();
		return fileMetaData;
	}

	/**
	 * Files will get saved to file system and database
	 * @param files
	 * @throws IOException
	 */
	private void saveUploadedFiles(List<MultipartFile> files) throws IOException {
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue; 
			}
			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
			Files.write(path, bytes);
			saveMetaData(file);

		}

	}

	/**
	 * Saves meta data to database
	 * @param file
	 * @throws IOException
	 */
	private void saveMetaData(MultipartFile file) throws IOException {
		FileUploadMetaData metaData = new FileUploadMetaData();
		metaData.setName(file.getOriginalFilename());
		metaData.setContentType(file.getContentType());
		metaData.setContentSize(file.getSize());
		fileUploadMetaData.save(metaData);
	}
}
