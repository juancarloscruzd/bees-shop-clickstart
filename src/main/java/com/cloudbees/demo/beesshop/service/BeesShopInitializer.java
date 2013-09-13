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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Component
public class BeesShopInitializer implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    JpaTransactionManager transactionManager;
    @PersistenceContext
    private EntityManager em;

    @Override
    public void afterPropertiesSet() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
        try {
            Query query = em.createQuery("select count(p) from Product p");
            long count = (Long) query.getSingleResult();
            if (count == 0) {

                logger.info("Database is empty, insert demo products");

                em.persist(new Product()
                        .withName("Buckwheat Honey - One pint")
                        .withDescription("Dark, full-bodied honey from New York state. Mildly sweet, with flavors of molasses, caramel, and smoke. Buckwheat honey is high in minerals and antioxidant compounds, and rumored to help ease coughing from upper respiratory infections.")
                        .withPriceInCents(1199)
                        .withProductUrl("http://www.huney.net/gourmet-honey/")
                        .withPhotoUrl("/img/buckwheat-honey.jpg")
                        .withPhotoCredit("http://huneynet.fatcow.com/store/media/BuckwheatHoney.jpg")
                        .withTags("honey"));

                em.persist(new Product()
                        .withName("Cotton Honey - One Pint")
                        .withDescription("Cotton honey from west Texas. This unique honey is naturally crystallized and spreadable like butter. It is very sweet with a mild creamy flavor and a clean, fresh smell. Our cotton honey is unstrained and contains pollen and small flecks of beeswax. ")
                        .withPriceInCents(1199)
                        .withProductUrl("http://www.huney.net/gourmet-honey/")
                        .withPhotoUrl("/img/cotton-honey.jpg")
                        .withPhotoCredit("http://huneynet.fatcow.com/store/media/West_Texas_Cotton.jpg")
                        .withTags("honey"));

            } else {
                logger.info("Products found in the database, don't insert new ones");
            }
            transactionManager.commit(status);
        } catch (RuntimeException e) {
            try {
                transactionManager.rollback(status);
            } catch (Exception rollbackException) {
                logger.warn("Silently ignore", rollbackException);
            }
            throw e;
        }
    }
}
