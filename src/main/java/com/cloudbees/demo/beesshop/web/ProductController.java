/*
 * Copyright 2010-2013, CloudBees Inc.
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

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.cloudbees.demo.beesshop.service.AmazonS3FileStorageService;
import com.cloudbees.demo.beesshop.service.MailService;
import com.cloudbees.demo.beesshop.domain.Product;
import com.cloudbees.demo.beesshop.domain.ProductRepository;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Controller
public class ProductController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AmazonS3FileStorageService fileStorageService;
    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "welcome";
    }

    @RequestMapping(value = "/product/{id}/comment", method = RequestMethod.POST)
    @Transactional
    public String addComment(@PathVariable long id, @RequestParam("comment") String comment, HttpServletRequest request) {

        Product product = productRepository.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        logger.debug("Add comment: '{}' to {}", comment, product);
        product.addComment(comment, request.getRemoteAddr());
        productRepository.update(product);

        return "redirect:/product/{id}";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/product/suggest")
    @ResponseBody
    public List<String> suggestProductWord(@RequestParam("term") String term) {
        List<String> words = this.productRepository.suggestProductWords(term);
        logger.trace("autocomplete word for {}:{}", term, words);
        return words;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/product/{id}")
    public String view(@PathVariable long id, Model model) {
        Product product = productRepository.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        model.addAttribute(product);

        return "product/view";
    }

    /**
     * @param id    id of the product
     * @param photo to associate with the product
     * @return redirection to display product
     */
    @RequestMapping(value = "/product/{id}/photo", method = RequestMethod.POST)
    @Transactional
    public String updatePhoto(@PathVariable long id, @RequestParam("photo") MultipartFile photo) {

        if (photo.getSize() == 0) {
            logger.info("Empty uploaded file");
        } else {
            try {
                String contentType = fileStorageService.findContentType(photo.getOriginalFilename());
                if (contentType == null) {
                    logger.warn("Skip file with unsupported extension '{}'", photo.getName());
                } else {

                    InputStream photoInputStream = photo.getInputStream();
                    long photoSize = photo.getSize();

                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    objectMetadata.setContentLength(photoSize);
                    objectMetadata.setContentType(contentType);
                    objectMetadata.setCacheControl("public, max-age=" + TimeUnit.SECONDS.convert(365, TimeUnit.DAYS));
                    String photoUrl = fileStorageService.storeFile(photoInputStream, objectMetadata);

                    Product product = productRepository.get(id);
                    logger.info("Saved {}", photoUrl);
                    product.setPhotoUrl(photoUrl);
                    productRepository.update(product);
                }

            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
        return "redirect:/product/" + id;
    }

    @Transactional
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST}, value = "/product/{id}")
    public String update(@PathVariable long id, @Valid Product product, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/product/{id}/edit";
        }

        product.setId(id);
        productRepository.update(product);

        return "redirect:/product/{id}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/product")
    public String create(@Valid Product product, BindingResult result) {
        if (result.hasErrors()) {
            return "product/create-form";
        }

        productRepository.insert(product);

        return "redirect:/product/" + product.getId();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/product")
    public ModelAndView find(@RequestParam(value = "name", required = false) String name) {

        Collection<Product> products = productRepository.find(name);

        logger.debug("find({}):{}", name, products);
        return new ModelAndView("product/view-all", "products", products);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/product/create-form")
    public String displayCreateForm(Model model) {
        model.addAttribute(new Product());
        return "product/create-form";
    }

    @RequestMapping(value = "/product/{id}/edit-form", method = RequestMethod.GET)
    public String displayEditForm(@PathVariable long id, Model model) {
        Product product = productRepository.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        model.addAttribute(product);
        return "product/edit-form";
    }

    @RequestMapping(value = "/product/{id}/mail", method = RequestMethod.POST)
    public String sendEmail(@PathVariable long id, @RequestParam("recipientEmail") String recipientEmail, HttpServletRequest request) {
        Product product = productRepository.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        try {
            String productPageUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath() + "/product/" + id;
            mailService.sendProductEmail(product, recipientEmail, productPageUrl);
        } catch (MessagingException e) {
            throw Throwables.propagate(e);
        }
        return "redirect:/product/" + product.getId();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Product not found")
    public static class ProductNotFoundException extends RuntimeException {

        private long id;

        public ProductNotFoundException(long id) {
            super("Resource: " + id);
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }
}
