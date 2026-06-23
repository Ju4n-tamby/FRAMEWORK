package com.framework.model;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UrlMethod) {
            return this.url.equals(((UrlMethod) o).url) && this.method.equals(((UrlMethod) o).method);
        }
        return false;
    }

    @Override
    public String toString() {
        return "UrlMethod{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
