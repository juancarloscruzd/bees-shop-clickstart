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

import com.cloudbees.demo.beesshop.domain.Product;
import com.cloudbees.demo.beesshop.domain.ShoppingCart;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JavaMail based mailer.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@ManagedResource("beesshop:type=MailService,name=MailService")
@Service
public class MailService {

    protected final Logger auditLogger = LoggerFactory.getLogger("audit");
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected InternetAddress fromAddress;
    protected AtomicInteger sentEmailCounter = new AtomicInteger();
    @Autowired
    private Session mailSession;

    @Required
    public void setFromAddress(String fromAddress) throws AddressException {
        this.fromAddress = new InternetAddress(fromAddress);
    }

    public void sendProductEmail(Product product, String recipient, String cocktailPageUrl) throws MessagingException {

        Message msg = new MimeMessage(mailSession);

        msg.setFrom(fromAddress);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        msg.setSubject("[BeesShop] Check this product: " + product.getName());
        String message = product.getName() + "\n" //
                + "--------------------\n" //
                + "\n" //
                + Strings.nullToEmpty(product.getDescription()) + "\n" //
                + "\n" //
                + cocktailPageUrl;
        msg.setContent(message, "text/plain");

        Transport.send(msg);
        auditLogger.info("Sent to {} product '{}'", recipient, product.getName());
        sentEmailCounter.incrementAndGet();
    }

    public void sendOrderConfirmation(ShoppingCart shoppingCart, String recipient) {

        try {
            Message msg = new MimeMessage(mailSession);

            msg.setFrom(fromAddress);
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            msg.setSubject("[BeesShop] Order Confirmation: " + shoppingCart.getItems() + " items - " + shoppingCart.getPrettyPrice());


            String message = "ORDER CONFIRMATION\n" +
                    "\n" +
                    "* Purchased items: " + shoppingCart.getItemsCount() + "\n" +
                    "* Price: " + shoppingCart.getPrettyPrice() + "\n";

            for (ShoppingCart.ShoppingCartItem item : shoppingCart.getItems()) {
                message += "   * " + item.getQuantity() + "x" + item.getProduct().getName() + "\n";
            }

            msg.setContent(message, "text/plain");

            Transport.send(msg);
            auditLogger.info("Sent to {} shopping cart with value of '{}'", recipient, shoppingCart.getPrettyPrice());
            sentEmailCounter.incrementAndGet();
        } catch (MessagingException e) {
            logger.warn("Exception sending order confirmation email to {}", recipient, e);
        }
    }

    @ManagedMetric(metricType = MetricType.COUNTER)
    public int getSentEmailCount() {
        return sentEmailCounter.get();
    }

}
