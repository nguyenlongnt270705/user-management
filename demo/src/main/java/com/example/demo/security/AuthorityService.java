package com.example.demo.security;

import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public Set<GrantedAuthority> loadAuthorities(Long userId) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String code : loadPermissionCodes(userId)) {
            authorities.add(new SimpleGrantedAuthority(code));
        }
        return authorities;
    }

    public Set<String> loadPermissionCodes(Long userId) {
        // SA có toàn bộ permission
        if (userRepository.isSystemAdmin(userId)) {
            return new HashSet<>(permissionRepository.findAllCodes());
        }
        // quyền của các role
        Set<String> codes = new HashSet<>(permissionRepository.findCodeByUserId(userId));
        // thêm bớt quyền
        codes.addAll(permissionRepository.findGrantedCodesByUserId(userId));
        permissionRepository.findRevokedCodesByUserId(userId).forEach(codes::remove);
        return codes;
    }
}
