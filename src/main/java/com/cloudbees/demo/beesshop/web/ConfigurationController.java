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
package com.cloudbees.demo.beesshop.web;

import com.cloudbees.demo.beesshop.service.AmazonS3FileStorageService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Controller
public class ConfigurationController {

    @Autowired
    private AmazonS3FileStorageService amazonS3FileStorageService;

    @RequestMapping(method = RequestMethod.GET, value = "/configuration")
    public String home() {
        return "configuration/index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/configuration/aws")
    public ModelAndView viewAwsConfiguration() {

        Map<String, Object> model = Maps.newHashMap();
        model.put("awsAccessKeyId", amazonS3FileStorageService.getAwsAccessKeyId());
        model.put("amazonS3BucketName", amazonS3FileStorageService.getAmazonS3BucketName());
        model.put("amazonS3BucketBasePublicUrl", amazonS3FileStorageService.getAmazonS3BucketBasePublicUrl());
        try {
            amazonS3FileStorageService.checkConfiguration();
        } catch (RuntimeException e) {
            model.put("warningException", e);
        }

        return new ModelAndView("configuration/aws", model);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/configuration/aws/credentials")
    public String updateAwsCredentials(
            @RequestParam("awsAccessKeyId") String awsAccessKeyId,
            @RequestParam("awsSecretKey") String awsSecretKey) {
        amazonS3FileStorageService.setAmazonCredentials(awsAccessKeyId, awsSecretKey);
        return "redirect:/configuration/aws";
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/configuration/aws/s3")
    public String updateAwsS3BucketConfiguration(
            @RequestParam("amazonS3BucketName") String amazonS3BucketName,
            @RequestParam("amazonS3BucketBasePublicUrl") String amazonS3BucketBasePublicUrl) {
        amazonS3FileStorageService.setAmazonS3BucketName(amazonS3BucketName);
        amazonS3FileStorageService.setAmazonS3BucketBasePublicUrl(amazonS3BucketBasePublicUrl);
        return "redirect:/configuration/aws";
    }
}
