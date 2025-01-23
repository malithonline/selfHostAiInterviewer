package com.aiinterviewpro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@CrossOrigin
public class DashboardController {

    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard/stats/email/{email}")
    public ResponseEntity<?> getDashboardStats(@PathVariable("email") String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Get stats individually
            Long totalInterviews = interviewRepository.countByUser(user);
            Double averageScore = interviewRepository.getAverageScore(user);
            Integer totalPracticeTime = interviewRepository.getTotalPracticeTime(user);
            
            List<Interview> recentInterviews = interviewRepository.findByUserOrderByDateTimeDesc(user);
            List<Interview> upcomingInterviews = interviewRepository
                .findByUserAndDateTimeGreaterThanOrderByDateTime(user, LocalDateTime.now());

            Map<String, Object> response = new HashMap<>();
            response.put("totalInterviews", totalInterviews);
            response.put("averageScore", Math.round(averageScore * 100.0) / 100.0); // Round to 2 decimal places
            response.put("totalPracticeTime", totalPracticeTime);
            response.put("recentInterviews", recentInterviews);
            response.put("upcomingInterviews", upcomingInterviews);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error getting stats: " + e.getMessage());
        }
    }


    @PostMapping("/dashboard/schedule/email/{email}")
    public ResponseEntity<?> scheduleInterview(@PathVariable("email") String email, @RequestBody Map<String, String> request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "User not found"
            ));
        }
    
        try {
            Interview interview = new Interview();
            interview.setUser(user);
            interview.setType(request.get("type"));
            interview.setDateTime(LocalDateTime.parse(request.get("dateTime")));
            interview.setDuration(Integer.parseInt(request.get("duration")));
            interview.setStatus("Scheduled");
            
            interview = interviewRepository.save(interview);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Interview scheduled successfully",
                "interviewId", interview.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Error scheduling interview: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/dashboard/complete/{interviewId}")
    public ResponseEntity<?> completeInterview(@PathVariable("interviewId") Long interviewId, @RequestBody Map<String, String> request) {
        Interview interview = interviewRepository.findById(interviewId).orElse(null);
        if (interview == null) {
            return ResponseEntity.badRequest().body("Interview not found");
        }

        try {
            interview.setScore(Integer.parseInt(request.get("score")));
            interview.setTranscript(request.get("transcript"));
            interview.setStatus("Completed");
            
            interviewRepository.save(interview);
            return ResponseEntity.ok("Interview completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error completing interview: " + e.getMessage());
        }
    }

    @PostMapping("/dashboard/cancel/{interviewId}")
    public ResponseEntity<?> cancelInterview(@PathVariable("interviewId") Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElse(null);
        if (interview == null) {
            return ResponseEntity.badRequest().body("Interview not found");
        }

        interview.setStatus("Cancelled");
        interviewRepository.save(interview);
        return ResponseEntity.ok("Interview cancelled successfully");
    }
}