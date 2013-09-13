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
package com.cloudbees.demo.beesshop.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

/**
 * http://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class MailSessionFactoryBean implements FactoryBean<Session>, InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private Properties smtpProperties;
    private Session mailSession;
    private String smtpUser;
    private String smtpPassword;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Strings.isNullOrEmpty(this.smtpUser)) {
            logger.info("Initialize anonymous mail session");
            mailSession = Session.getInstance(smtpProperties);
        } else {
            logger.info("Initialize mail session with user='{}', password='xxx'", smtpUser);

            smtpProperties.setProperty("mail.smtp.auth", "true");
            mailSession = Session.getInstance(smtpProperties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });
        }
    }

    @Override
    public Session getObject() throws Exception {
        return mailSession;
    }

    @Override
    public Class<?> getObjectType() {
        return Session.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setSmtpProperties(Properties smtpProperties) {
        this.smtpProperties = smtpProperties;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }
}
