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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Entity
public class Comment implements Serializable, Comparable<Comment> {
    @Id
    @GeneratedValue
    Long id;
    @Nonnull
    String message;
    @Nonnull
    String remoteIp;
    @Nonnull
    Timestamp timestamp;

    public Comment() {
    }

    public Comment(String message, String remoteIp, Timestamp timestamp) {
        this.message = message;
        this.remoteIp = remoteIp;
        this.timestamp = timestamp;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    public void setMessage(@Nonnull String comment) {
        this.message = comment;
    }

    @Nonnull
    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(@Nonnull String remoteIp) {
        this.remoteIp = remoteIp;
    }

    @Nonnull
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Nonnull
    public String getPrettyTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
        return sdf.format(getTimestamp());
    }

    public void setTimestamp(@Nonnull Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;

        Comment comment = (Comment) o;

        if (id != null ? !id.equals(comment.id) : comment.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * Sort by {@link #timestamp}
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Comment other) {
        if (this.timestamp == null) {
            return other.timestamp == null ? 0 : -1;
        }
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", comment='" + message + '\'' +
                ", remoteIp='" + remoteIp + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
