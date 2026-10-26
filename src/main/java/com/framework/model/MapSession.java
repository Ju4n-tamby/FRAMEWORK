package com.framework.model;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MapSession {
    private final Map<String, Object> data = new HashMap<String, Object>();
    private final Set<String> clesInitiales = new HashSet<String>();

    public MapSession() {
    }

    public static MapSession from(HttpSession session) {
        MapSession map = new MapSession();
        for (String nom : java.util.Collections.list(session.getAttributeNames())) {
            map.data.put(nom, session.getAttribute(nom));
            map.clesInitiales.add(nom);
        }
        return map;
    }

    public Object getAttribute(String nom) {
        return this.data.get(nom);
    }

    public void setAttribute(String nom, Object value) {
        Objects.requireNonNull(nom, "Le nom de l attribut de session ne peut pas être null");
        if (value == null) {
            this.data.remove(nom);
        } else {
            this.data.put(nom, value);
        }
    }

    public void removeAttribute(String nom) {
        this.data.remove(nom);
    }

    public boolean contains(String nom) {
        return this.data.containsKey(nom);
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public void applyTo(HttpSession session) {
        for (String nom : this.clesInitiales) {
            if (!this.data.containsKey(nom)) {
                session.removeAttribute(nom);
            }
        }
        for (Map.Entry<String, Object> entry : this.data.entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
        this.clesInitiales.clear();
        this.clesInitiales.addAll(this.data.keySet());
    }
}
