package com.example.kakapupu;

public class Service {
    private String serviceId;
    private String name;
    private String description;
    private String category;

    public Service() {}

    public Service(String serviceId, String name, String description, String category) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getServiceId() { return serviceId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
}