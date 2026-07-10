package com.tool.aidubbing.repository;

import com.tool.aidubbing.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByUserId(long userId);

    Optional<Transaction> findByUserId(long userId);

}