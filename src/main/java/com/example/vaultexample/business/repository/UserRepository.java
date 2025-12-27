package com.example.vaultexample.business.repository;

import com.example.vaultexample.business.entity.Authorites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface UserRepository extends JpaRepository<Authorites, Integer> {

}
