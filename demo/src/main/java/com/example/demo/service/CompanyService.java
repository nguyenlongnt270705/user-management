package com.example.demo.service;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.SearchResponse;
import com.example.demo.entity.Company;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorMessage;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.security.PrivilegeGuard;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PrivilegeGuard privilegeGuard;

    public SearchResponse<CompanyDTO> search(SearchRequest<SearchCompanyReq> request) {
        SearchRequest<SearchCompanyReq> safeRequest = request != null ? request : new SearchRequest<>();
        Specification<Company> spec = specification(safeRequest.getFilter());

        int page = safeRequest.getPagination() != null && safeRequest.getPagination().getPage() != null
                ? Math.max(safeRequest.getPagination().getPage(), 1) - 1
                : 0;
        int size = safeRequest.getPagination() != null && safeRequest.getPagination().getSize() != null
                ? Math.min(Math.max(safeRequest.getPagination().getSize(), 1), 100)
                : 10;

        Pageable pageable;
        SearchRequest.Sort sort = safeRequest.getSort();
        if (sort != null && StringUtils.hasText(sort.getField()) && StringUtils.hasText(sort.getOrder())) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sort.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageable = PageRequest.of(page, size, Sort.by(direction, sort.getField()));
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        }

        Page<Company> pageResult = companyRepository.findAll(spec, pageable);
        List<CompanyDTO> data = pageResult.getContent().stream()
                .map(CompanyDTO::fromEntity)
                .toList();
        return new SearchResponse<>(data, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public CompanyDTO getById(Long id) {
        return CompanyDTO.fromEntity(findCompany(id));
    }

    @Transactional
    public CompanyDTO create(CreateCompanyRequest request) {
        String code = normalizeCode(request.code());
        if (companyRepository.findByCode(code).isPresent()) {
            throw AppException.error(ErrorMessage.COMPANY_CODE_EXISTS);
        }

        try {
            Company company = Company.builder()
                    .code(code)
                    .name(request.name().trim())
                    .address(request.address().trim())
                    .description(request.description())
                    .build();
            return CompanyDTO.fromEntity(companyRepository.saveAndFlush(company));
        } catch (DataIntegrityViolationException e) {
            throw AppException.error(ErrorMessage.COMPANY_CODE_EXISTS);
        }
    }

    @Transactional
    public CompanyDTO update(Long id, UpdateCompanyRequest request) {
        Company company = findCompany(id);
        company.setName(request.name().trim());
        company.setAddress(request.address().trim());
        company.setDescription(request.description());
        return CompanyDTO.fromEntity(companyRepository.saveAndFlush(company));
    }

    @Transactional
    public CompanyDTO changeStatus(Long id, ChangeCompanyStatusRequest request) {
        Company company = findCompany(id);
        privilegeGuard.assertCanChangeCompanyStatus(company, request.status());

        company.setStatus(request.status());
        return CompanyDTO.fromEntity(companyRepository.saveAndFlush(company));
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> AppException.error(ErrorMessage.COMPANY_NOT_FOUND));
    }

    private Specification<Company> specification(SearchCompanyReq filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (StringUtils.hasText(filter.code())) {
                    predicates.add(cb.equal(root.get("code"), normalizeCode(filter.code())));
                }
                if (StringUtils.hasText(filter.name())) {
                    predicates.add(cb.equal(root.get("name"), filter.name().trim()));
                }
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.status()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
