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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Entity
public class Product implements Comparable<Product>, Serializable {

    private String description;
    private String name;
    @Id
    @GeneratedValue
    private Long id;
    private String photoUrl;
    private String photoCredit;
    /**
     * URL of the product recipe that has been used
     */
    private String productUrl;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> tags = new TreeSet<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<Comment> comments = new ArrayList<>();
    private int priceInCents;

    public String getDescriptionAsHtml() {
        return description == null ? "" : description.replace("\n", "<br />\n");
    }

    @Override
    public int compareTo(Product other) {
        if (this.name == null) {
            return other.name == null ? 0 : -1;
        }
        return this.name.compareTo(other.name);
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Product withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Product withPhotoCredit(String photoCredit) {
        this.photoCredit = photoCredit;
        return this;
    }

    public String getPhotoCredit() {
        return photoCredit;
    }

    public void setPhotoCredit(String photoCredit) {
        this.photoCredit = photoCredit;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String recipeUrl) {
        this.productUrl = recipeUrl;
    }

    public void addComment(String comment, String remoteIp) {
        comments.add(new Comment(comment, remoteIp, new Timestamp(System.currentTimeMillis())));
    }

    /**
     * @return read only
     */
    @Nonnull
    public ImmutableList<Comment> getComments() {
        return ImmutableList.copyOf(Lists.reverse(comments));
    }

    public Product withId(@Nullable Long id) {
        setId(id);
        return this;
    }

    public Product withName(@Nullable String name) {
        setName(name);
        return this;
    }

    public Product withPhotoUrl(@Nullable String photoUrl) {
        setPhotoUrl(photoUrl);
        return this;
    }

    public Product withProductUrl(String productUrl) {
        setProductUrl(productUrl);
        return this;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    public void setPriceInCents(int priceInCents) {
        this.priceInCents = priceInCents;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Product withTags(String... tags) {
        for (String tag : tags) {
            this.tags.add(tag);
        }
        return this;
    }

    /**
     * Price in dollars, empty {@link String} if price is <code>null</code>
     */
    @Nonnull
    public String getPrettyPrice() {
        BigDecimal priceInDollars = new BigDecimal(getPriceInCents()).movePointLeft(2);
        return NumberFormat.getCurrencyInstance(Locale.US).format(priceInDollars);
    }

    public Product withPriceInCents(Integer priceInCents) {
        this.priceInCents = priceInCents;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        if (id != product.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
