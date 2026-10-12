package com.example.demo.repository;

import com.example.demo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    @Query("""
            select distinct p.code from User u join u.roles r join r.permissions p where u.id = :userId
            """)
    List<String> findCodeByUserId(@Param("userId") Long userId);

    @Query("""
            select p.code from Permission p
            """)
    List<String> findAllCodes();

    @Query("select p.code from User u join u.grantedPermissions p where u.id = :userId")
    List<String> findGrantedCodesByUserId(@Param("userId") Long userId);

    @Query("select p.code from User u join u.revokedPermissions p where u.id = :userId")
    List<String> findRevokedCodesByUserId(@Param("userId") Long userId);

    @Query("select p from Permission p where p.code in :codes")
    List<Permission> findByCodeIn(@Param("codes") Collection<String> codes);
}
