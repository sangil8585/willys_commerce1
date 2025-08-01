package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductJpaRepositoryImpl implements ProductJpaRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<ProductEntity> findAll(ProductCriteria criteria, Pageable pageable) {
        QueryBuilder queryBuilder = new QueryBuilder();
        
        queryBuilder.addWhere("p.deletedAt IS NULL");
        
        if (criteria != null && criteria.criteria() != null) {
            for (ProductCriteria.Criterion criterion : criteria.criteria()) {
                processCriterion(criterion, queryBuilder);
            }
        }
        
        List<String> orderByClauses = buildOrderByClauses(criteria);
        
        String jpql = buildMainQuery(queryBuilder.getWhereClauses(), orderByClauses);
        
        String countJpql = buildCountQuery(queryBuilder.getWhereClauses());
        
        TypedQuery<ProductEntity> query = entityManager.createQuery(jpql, ProductEntity.class);
        setParameters(query, queryBuilder.getParameters());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<ProductEntity> content = query.getResultList();
        
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setParameters(countQuery, queryBuilder.getParameters());
        Long total = countQuery.getSingleResult();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private void processCriterion(ProductCriteria.Criterion criterion, QueryBuilder queryBuilder) {
        if (criterion instanceof ProductCriteria.NameContains nameContains) {
            if (StringUtils.hasText(nameContains.name())) {
                queryBuilder.addWhere("LOWER(p.name) LIKE LOWER(:name)");
                queryBuilder.addParameter("name", "%" + nameContains.name() + "%");
            }
        } else if (criterion instanceof ProductCriteria.BrandIdEquals brandIdEquals) {
            if (brandIdEquals.brandId() != null) {
                queryBuilder.addWhere("p.brandId = :brandId");
                queryBuilder.addParameter("brandId", brandIdEquals.brandId());
            }
        } else if (criterion instanceof ProductCriteria.PriceRange priceRange) {
            if (priceRange.minPrice() != null) {
                queryBuilder.addWhere("p.price >= :minPrice");
                queryBuilder.addParameter("minPrice", priceRange.minPrice());
            }
            if (priceRange.maxPrice() != null) {
                queryBuilder.addWhere("p.price <= :maxPrice");
                queryBuilder.addParameter("maxPrice", priceRange.maxPrice());
            }
        } else if (criterion instanceof ProductCriteria.StockGreaterThan stockGreaterThan) {
            if (stockGreaterThan.stock() != null) {
                queryBuilder.addWhere("p.stock > :stock");
                queryBuilder.addParameter("stock", stockGreaterThan.stock());
            }
        } else if (criterion instanceof ProductCriteria.LikesGreaterThan likesGreaterThan) {
            if (likesGreaterThan.likes() != null) {
                queryBuilder.addWhere("p.likes > :likes");
                queryBuilder.addParameter("likes", likesGreaterThan.likes());
            }
        }
    }
    
    private List<String> buildOrderByClauses(ProductCriteria criteria) {
        List<String> orderByClauses = new ArrayList<>();
        
        if (criteria != null && criteria.criteria() != null) {
            for (ProductCriteria.Criterion criterion : criteria.criteria()) {
                if (criterion instanceof ProductCriteria.OrderByCreatedAt orderByCreatedAt) {
                    orderByClauses.add("p.createdAt " + (orderByCreatedAt.ascending() ? "ASC" : "DESC"));
                } else if (criterion instanceof ProductCriteria.OrderByPrice orderByPrice) {
                    orderByClauses.add("p.price " + (orderByPrice.ascending() ? "ASC" : "DESC"));
                } else if (criterion instanceof ProductCriteria.OrderByLikeCount) {
                    orderByClauses.add("p.likes DESC");
                }
            }
        }
        
        if (orderByClauses.isEmpty()) {
            orderByClauses.add("p.createdAt DESC");
        }
        
        return orderByClauses;
    }
    
    private String buildMainQuery(List<String> whereClauses, List<String> orderByClauses) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM ProductEntity p");
        
        if (!whereClauses.isEmpty()) {
            jpql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }
        
        if (!orderByClauses.isEmpty()) {
            jpql.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        }
        
        return jpql.toString();
    }
    
    private String buildCountQuery(List<String> whereClauses) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM ProductEntity p");
        
        if (!whereClauses.isEmpty()) {
            jpql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }
        
        return jpql.toString();
    }
    
    private void setParameters(TypedQuery<?> query, List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            query.setParameter(parameter.name(), parameter.value());
        }
    }
    
    private static class QueryBuilder {
        private final List<String> whereClauses = new ArrayList<>();
        private final List<Parameter> parameters = new ArrayList<>();
        
        public void addWhere(String whereClause) {
            whereClauses.add(whereClause);
        }
        
        public void addParameter(String name, Object value) {
            parameters.add(new Parameter(name, value));
        }
        
        public List<String> getWhereClauses() {
            return whereClauses;
        }
        
        public List<Parameter> getParameters() {
            return parameters;
        }
    }
    
    private static class Parameter {
        private final String name;
        private final Object value;
        
        public Parameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }
        
        public String name() {
            return name;
        }
        
        public Object value() {
            return value;
        }
    }
} 