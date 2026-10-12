package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query("select u from User u join fetch u.company where u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select u from User u join fetch u.company where u.phoneNumber = :phone")
    Optional<User> findByPhoneNumber(@Param("phone") String phone);

    @Query("select u from User u join fetch u.company where u.id = :id")
    Optional<User> findWithCompanyById(@Param("id") Long id);

    @Query("select u from User u join fetch u.company left join fetch u.roles where u.id = :id")
    Optional<User> findDetailById(@Param("id") Long id);

    @Query("""
            select case when count(u) > 0 then true else false end
            from User u join u.roles r
            where u.id = :userId and r.systemAdmin = true
            """)
    boolean isSystemAdmin(@Param("userId") Long userId);

    @Query("select case when count(u) > 0 then true else false end from User u where u.userCode = :userCode")
    boolean existsByUserCode(@Param("userCode") String userCode);

    @Query("select case when count(u) > 0 then true else false end from User u where u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("select case when count(u) > 0 then true else false end from User u where u.phoneNumber = :phone")
    boolean existsByPhoneNumber(@Param("phone") String phone);
}
