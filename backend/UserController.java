package com.aiinterviewpro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // This is the main change - allows all origins
@RequestMapping("/auth")    // This matches your frontend API calls
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userRepository.findByEmail(user.getEmail()) != null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Email already exists"
                ));
            }
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registration successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Registration failed"
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "token", "dummy-token-" + System.currentTimeMillis(),
                "user", existingUser
            ));
        }
        return ResponseEntity.ok(Map.of(  // Changed from badRequest() to ok()
            "success", false,
            "message", "Invalid email or password"
        ));
    }

    // Find by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        return ResponseEntity.ok(user);
    }

    // Find by Email
    @GetMapping("/users/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable("email") String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        return ResponseEntity.ok(user);
    }

    // Update by ID
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateName(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            user.setFullName(request.get("fullName"));
            userRepository.save(user);
            return ResponseEntity.ok("Name updated successfully to: " + request.get("fullName"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update by Email
    @PutMapping("/users/email/{email}")
    public ResponseEntity<?> updateNameByEmail(@PathVariable("email") String email, @RequestBody Map<String, String> request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            user.setFullName(request.get("fullName"));
            userRepository.save(user);
            return ResponseEntity.ok("Name updated successfully to: " + request.get("fullName"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}