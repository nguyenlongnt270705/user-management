package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.ValidationPatterns;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    // find by email/phone
    @Override
    public UserDetails loadUserByUsername(String identifier) {
        String input = identifier == null ? "" : identifier.trim();

        Optional<User> found;
        if (input.contains("@")) {
            found = userRepository.findByEmail(input.toLowerCase(Locale.ROOT));
        } else if (input.matches(ValidationPatterns.PHONE)) {
            found = userRepository.findByPhoneNumber(input);
        } else {
            // định dạng không hợp lệ
            found = Optional.empty();
        }

        User user = found.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + identifier));

        return new CustomUserDetails(user);
    }
}
