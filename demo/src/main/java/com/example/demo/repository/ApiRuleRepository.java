package com.example.demo.repository;

import com.example.demo.entity.ApiRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiRuleRepository extends JpaRepository<ApiRule, Long>, JpaSpecificationExecutor<ApiRule> {

    @Query("select r from ApiRule r join fetch r.permission")
    List<ApiRule> findAllWithPermission();

    // Tìm rule theo method + path
    @Query("select r from ApiRule r where r.httpMethod = :method and r.pathPattern = :path")
    Optional<ApiRule> findByMethodAndPath(@Param("method") String method, @Param("path") String path);

    // tìm rule kèm permission
    @Query("select r from ApiRule r join fetch r.permission where r.id = :id")
    Optional<ApiRule> findDetailById(@Param("id") Long id);
}
