# AWS S3 File Operations ( Upload/Download/Delete Files)

# Prerequisites:
#### Step 1: Create Spring Boot Project
* Dependencies - web, aws

#### Step 2: Configure AWS Access Key/Secret Key
```
awsAccessKey=youraccesskey
awsSecretKey=yoursecretkey
```

#### Step 3: Create S3Util
```
package com.naresh.s3uploaddownloadapi;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

@Component
public class S3Util {

	private static final String bucketName = "naresh-test-829268275261";

	private AmazonS3 s3Client;

	@Value("${awsAccessKey}")
	private String accessKey;

	@Value("${awsSecretKey}")
	private String secretKey;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.US_EAST_1).build();

	}

	public void uploadPhoto(String key, File file) {
		this.s3Client.putObject(bucketName, key, file);
	}

	public byte[] downloadPhoto(String key) {

		S3Object s3object = this.s3Client.getObject(bucketName, key);
		S3ObjectInputStream inputStream = s3object.getObjectContent();
		byte[] byteArray = null;
		try {
			byteArray = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteArray;
	}

	public void deleteFile(String key) {
		this.s3Client.deleteObject(bucketName, key);
		
	}
}
```

#### Step 4: Controller
```
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
```
