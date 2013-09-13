/*
 * Copyright 2010-2013, the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudbees.demo.beesshop.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Amazon AWS S3 storage for images.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Service
public class AmazonS3FileStorageService implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Random random = new Random();
    private String amazonS3BucketName;
    private String amazonS3BucketBasePublicUrl;
    private AWSCredentials awsCredentials;
    private AmazonS3 amazonS3;
    private Map<String, String> contentTypeByFileExtension = new HashMap<String, String>() {
        {
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("gif", "image/gif");
        }
    };
    private Map<String, String> defaultFileExtensionByContentType = new HashMap<String, String>() {
        {
            put("image/jpeg", "jpg");
            put("image/png", "png");
            put("image/gif", "gif");
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        this.amazonS3 = new AmazonS3Client(awsCredentials);

        try {
            checkConfiguration();
        } catch (RuntimeException e) {
            logger.warn("Amazon S3 configuration problem: " + e.getMessage(), e);
        }
    }

    public void setAmazonCredentials(String awsAccessKey, String awsSecretKey) {
        this.awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        this.amazonS3 = new AmazonS3Client(awsCredentials);
    }

    public void checkConfiguration() throws RuntimeException {
        try {
            if (!amazonS3.doesBucketExist(amazonS3BucketName)) {
                throw new RuntimeException("Bucket '" + amazonS3BucketName + "' not found for user '" + awsCredentials.getAWSAccessKeyId() + "'");
            }
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 403 && "SignatureDoesNotMatch".equals(e.getErrorCode())) {
                throw new RuntimeException("Invalid credentials AWSAccessKeyId='" + awsCredentials.getAWSAccessKeyId() + "', AWSSecretKey=****", e);
            } else {
                throw e;
            }
        }
    }

    @Nullable
    public String findContentType(String fileName) {
        String fileExtension = Iterables.getLast(Splitter.on('.').split(fileName), null);
        fileExtension = Strings.nullToEmpty(fileExtension).toLowerCase();
        return contentTypeByFileExtension.get(fileExtension);
    }

    /**
     * @param in             bytes to store
     * @param objectMetadata Amazon S3 metadata
     * @return Amazon S3 URL
     */
    @Nonnull
    public String storeFile(InputStream in, ObjectMetadata objectMetadata) {
        String extension = defaultFileExtensionByContentType.get(objectMetadata.getContentType());
        String fileName = Joiner.on(".").skipNulls().join(Math.abs(random.nextLong()), extension);
        amazonS3.putObject(amazonS3BucketName, fileName, in, objectMetadata);

        return amazonS3BucketBasePublicUrl + fileName;
    }

    public String getAwsAccessKeyId() {
        return awsCredentials.getAWSAccessKeyId();
    }

    public String getAmazonS3BucketName() {
        return amazonS3BucketName;
    }

    @Required
    public void setAmazonS3BucketName(String amazonS3BucketName) {
        this.amazonS3BucketName = amazonS3BucketName;
    }

    public String getAmazonS3BucketBasePublicUrl() {
        return amazonS3BucketBasePublicUrl;
    }

    @Required
    public void setAmazonS3BucketBasePublicUrl(String amazonS3BucketBasePublicUrl) {
        this.amazonS3BucketBasePublicUrl = amazonS3BucketBasePublicUrl;
    }

    @Required
    public void setAwsCredentials(AWSCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
        this.amazonS3 = new AmazonS3Client(awsCredentials);
    }
}
