package com.example.freelancer.repository;

import com.example.freelancer.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job,Integer> {
    @Query("SELECT j FROM Job j WHERE j.freelancerId = :freelancerId AND j.status = 4")
    List<Job> getTotalJobDone(@Param(value="freelancerId") Integer freelancerId);

    @Query("SELECT j FROM Job j WHERE j.freelancerId = :freelancerId")
    List<Job> getListJobByFreelancerId(Integer freelancerId);

    @Query("SELECT j FROM Job j WHERE j.accountId = :accountId")
    List<Job> getListJobByAccountId(Integer accountId);

    @Query("SELECT j FROM Job j WHERE j.accountId = :accountId AND j.freelancerId = :freelancerId")
    List<Job> getListJobByAccountIdAndFreelancerId(Integer accountId, Integer freelancerId);

    @Query("SELECT j FROM Job j ORDER BY created_at DESC")
    Page<Job> findAll(Pageable pageable);
}
