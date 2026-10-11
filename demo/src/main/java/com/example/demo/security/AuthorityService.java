package com.example.demo.security;

import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public Set<GrantedAuthority> loadAuthorities(Long userId) {
        List<String> codes;
        if (userRepository.isSystemAdmin(userId)) {
            // SA
            codes = permissionRepository.findAllCodes();
        } else {
            // user thường
            codes = permissionRepository.findCodeByUserId(userId);
        }

        // convert permissionCode -> Authorities <=> @PreAuthorize
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String code : codes) {
            authorities.add(new SimpleGrantedAuthority(code));
        }
        return authorities;
    }
}
