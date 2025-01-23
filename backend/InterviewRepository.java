package com.aiinterviewpro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.time.LocalDateTime;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserOrderByDateTimeDesc(User user);
    List<Interview> findByUserAndDateTimeGreaterThanOrderByDateTime(User user, LocalDateTime date);
    
    // Separate queries for each stat
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.user = ?1")
    Long countByUser(User user);
    
    @Query("SELECT COALESCE(AVG(i.score), 0) FROM Interview i WHERE i.user = ?1 AND i.score IS NOT NULL")
    Double getAverageScore(User user);
    
    @Query("SELECT COALESCE(SUM(i.duration), 0) FROM Interview i WHERE i.user = ?1")
    Integer getTotalPracticeTime(User user);
}