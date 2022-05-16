package com.hvl.dragonteam.Model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class ImageUploadResult {

    String result;
    String url;

    public ImageUploadResult() {
    }

    public ImageUploadResult(String result, String url) {
        this.result = result;
        this.url = url;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object that) {
        boolean isEqual = EqualsBuilder.reflectionEquals(this, that);
        return isEqual;
    }

    @Override
    public int hashCode() {
        int hashCode = HashCodeBuilder.reflectionHashCode(this);
        return hashCode;
    }

    @Override
    public String toString() {
        String str = ReflectionToStringBuilder.toString(this);
        return str;
    }
}
