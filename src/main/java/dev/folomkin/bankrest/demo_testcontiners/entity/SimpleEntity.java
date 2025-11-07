package dev.folomkin.bankrest.demo_testcontiners.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class SimpleEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    // Конструкторы
    public SimpleEntity() {
    }

    public SimpleEntity(String name) {
        this.name = name;
    }

    // Геттеры/сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}