package com.example.demo.security;

import com.example.demo.entity.ApiRule;
import com.example.demo.repository.ApiRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiRuleRegistry {

    private final ApiRuleRepository apiRuleRepository;
    private final PathPatternParser parser = createParser();
    private volatile List<CompiledRule> rules = List.of();

    public record CompiledRule(
            String httpMethod,
            PathPattern pattern,
            String permissionCode,
            boolean locked
    ) {
    }

    // get rule khi boot, khi rule thay đổi
    @PostConstruct
    public void reload() {
        List<CompiledRule> compiled = new ArrayList<>();
        for (ApiRule rule : apiRuleRepository.findAllWithPermission()) {
            compiled.add(new CompiledRule(
                    rule.getHttpMethod(),
                    parser.parse(rule.getPathPattern()),
                    rule.getPermission().getCode(),
                    rule.isLocked()));
        }

        // rule cụ thể nhất (/create > /{id}
        compiled.sort(Comparator
                // reversed order vì false đúng trước true
                .comparing(CompiledRule::locked, Comparator.reverseOrder())
                .thenComparing(CompiledRule::pattern, PathPattern.SPECIFICITY_COMPARATOR));
        rules = List.copyOf(compiled);
    }

    // tìm rule khớp method và path
    public Optional<CompiledRule> match(String method, String path) {
        PathContainer container = PathContainer.parsePath(path);
        for (CompiledRule rule : rules) {
            if (rule.httpMethod().equalsIgnoreCase(method) && rule.pattern().matches(container)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    private static PathPatternParser createParser() {
        return new PathPatternParser();
    }
}
