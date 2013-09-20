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
package com.cloudbees.demo.beesshop.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Repository
public class ProductRepository {
    final Logger logger = LoggerFactory.getLogger(getClass());
    @PersistenceContext
    private EntityManager em;

    @Nonnull
    public Collection<Product> find(@Nullable String name) {
        Query query;
        if (name == null || name.isEmpty()) {
            query = em.createQuery("select p from Product p");
        } else {
            query = em.createQuery("select p from Product p where p.name=:name");
            query.setParameter("name", name);
        }
        List result = query.getResultList();

        logger.trace("find({}): {}", name, result);
        return result;
    }

    @Nullable
    public Product get(long id) {
        return em.find(Product.class, id);
    }

    public List<String> suggestProductKeywords(String query) {
        return Collections.singletonList("#TODO#");
    }

    public List<String> suggestProductWords(String query) {
        return Collections.singletonList("#TODO#");
    }

    @Transactional
    public Product update(@Nonnull Product product) {
        return em.merge(product);
    }

    @Transactional
    public void insert(@Nonnull Product product) {
        em.persist(product);
    }
}
