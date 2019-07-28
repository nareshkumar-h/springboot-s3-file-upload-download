package com.naresh.s3uploaddownloadapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	private S3Util s3Util;
	
	@PostMapping("upload")
	public void uploadFile(@RequestParam("file") MultipartFile file) {
		System.out.println("Upload-" + file);
		try {
			File fileObj = convertMultiPartToFile(file);
			String key = file.getOriginalFilename();
			s3Util.uploadPhoto(key, fileObj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@GetMapping("download")
	public ResponseEntity<byte[]> downloadFile(@RequestParam("fileName") String fileName) {
		System.out.println("Download-" + fileName);
		byte[] content = null;
		try {
			String key = fileName;
			content= s3Util.downloadPhoto(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" +fileName + "\"").body(content);
		
	}
	
	@PostMapping("delete")
	public void deleteFile(@RequestParam("fileName") String fileName) {
		System.out.println("Delete-" + fileName);
		try {
			String key = fileName;
			s3Util.deleteFile(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
	    File convFile = new File(file.getOriginalFilename());
	    FileOutputStream fos = new FileOutputStream(convFile);
	    fos.write(file.getBytes());
	    fos.close();
	    return convFile;
	}
}
