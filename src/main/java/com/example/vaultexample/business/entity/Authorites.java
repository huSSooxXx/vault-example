package com.example.vaultexample.business.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Table(name = "authorities", schema = "public")
@Data
public class Authorites {
    @Id
    private Integer id;
    private String username;
    private String authority;
}