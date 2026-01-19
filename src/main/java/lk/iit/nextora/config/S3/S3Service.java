package lk.iit.nextora.config.S3;

import lk.iit.nextora.common.util.FileUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    /**
     * -- GETTER --
     *  Get the bucket name
     *
     * @return The configured bucket name
     */
    @Getter
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload a file to S3
     *
     * @param file   The file to upload
     * @param folder The folder path in S3 (e.g., "uploads/images")
     * @return The S3 key (path) of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = FileUtils.generateUniqueFileName(file.getOriginalFilename());
            String key = folder + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("File uploaded successfully to S3: {}", key);
            return key;
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Upload a file to S3 with public read access
     *
     * @param file   The file to upload
     * @param folder The folder path in S3
     * @return The public URL of the uploaded file
     */
    public String uploadFilePublic(MultipartFile file, String folder) {
        try {
            String fileName = FileUtils.generateUniqueFileName(file.getOriginalFilename());
            String key = folder + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("File uploaded successfully to S3 with public access: {}", key);
            return getPublicUrl(key);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Delete a file from S3
     *
     * @param key The S3 key (path) of the file to delete
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * Check if a file exists in S3
     *
     * @param key The S3 key (path) of the file
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Get the public URL for an S3 object
     *
     * @param key The S3 key (path) of the file
     * @return The public URL
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Download file bytes from S3
     *
     * @param key The S3 key (path) of the file
     * @return The file content as byte array
     */
    public byte[] downloadFile(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (S3Exception e) {
            log.error("Failed to download file from S3: {}", key, e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }


}
