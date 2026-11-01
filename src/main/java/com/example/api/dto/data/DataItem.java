package com.example.api.dto.data;

public class DataItem {

    private final Long id;
    private final String title;

    public DataItem(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
