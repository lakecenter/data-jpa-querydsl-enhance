package com.sky.data.jpa.support.querydsl.enhance;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.*;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuerydslJpaPredicate2Executor<T>
    implements
    QuerydslPredicate2Executor<T> {


    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityPath<T> path;
    private final Querydsl querydsl;
    private final EntityManager entityManager;
    private final CrudMethodMetadata metadata;

    public QuerydslJpaPredicate2Executor(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager,
                                         EntityPathResolver resolver, @Nullable CrudMethodMetadata metadata) {

        this.entityInformation = entityInformation;
        this.metadata = metadata;
        this.path = resolver.createPath(entityInformation.getJavaType());
        this.querydsl = new Querydsl(entityManager, new PathBuilder<T>(path.getType(), path.getMetadata()));
        this.entityManager = entityManager;
    }

    @Override
    public <E> Optional<E> findOne(Predicate predicate, ConstructorExpression<E> ce) {
        try {
            return Optional.ofNullable(createQuery(predicate).select(ce).fetchOne());
        } catch (NonUniqueResultException ex) {
            throw new IncorrectResultSizeDataAccessException(ex.getMessage(), 1, ex);
        }
    }

    @Override
    public <E> Iterable<E> findAll(Predicate predicate, ConstructorExpression<E> ce) {
        return createQuery(predicate).select(ce).fetch();
    }

    @Override
    public <E> Iterable<E> findAll(Predicate predicate, Sort sort, ConstructorExpression<E> ce) {
        return executeSorted(createQuery(predicate).select(ce), sort);
    }

    @Override
    public <E> Iterable<E> findAll(Predicate predicate, ConstructorExpression<E> ce, OrderSpecifier<?>... orders) {
        return executeSorted(createQuery(predicate).select(ce), orders);
    }

    @Override
    public <E> Iterable<E> findAll(ConstructorExpression<E> ce, OrderSpecifier<?>... orders) {
        Assert.notNull(orders, "Order specifiers must not be null!");

        return executeSorted(createQuery(new Predicate[0]).select(ce), orders);
    }

    @Override
    public <E> Page<E> findAll(Predicate predicate, Pageable pageable, ConstructorExpression<E> ce) {
        Assert.notNull(pageable, "Pageable must not be null!");

        final JPQLQuery<?> countQuery = createCountQuery(predicate);
        JPQLQuery<E> query = querydsl.applyPagination(pageable, createQuery(predicate).select(ce));

        return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchCount);
    }

    /**
     * Creates a new {@link JPQLQuery} for the given {@link Predicate}.
     *
     * @param predicate
     * @return the Querydsl {@link JPQLQuery}.
     */
    protected JPQLQuery<?> createQuery(Predicate... predicate) {

        AbstractJPAQuery<?, ?> query = doCreateQuery(getQueryHints().withFetchGraphs(entityManager), predicate);

        CrudMethodMetadata metadata = getRepositoryMethodMetadata();

        if (metadata == null) {
            return query;
        }

        LockModeType type = metadata.getLockModeType();
        return type == null ? query : query.setLockMode(type);
    }

    @Nullable
    protected CrudMethodMetadata getRepositoryMethodMetadata() {
        return metadata;
    }

    /**
     * Creates a new {@link JPQLQuery} count query for the given {@link Predicate}.
     *
     * @param predicate, can be {@literal null}.
     * @return the Querydsl count {@link JPQLQuery}.
     */
    protected JPQLQuery<?> createCountQuery(@Nullable Predicate... predicate) {
        return doCreateQuery(getQueryHints(), predicate);
    }

    /**
     * Returns {@link QueryHints} with the query hints based on the current {@link CrudMethodMetadata} and potential
     * {@link EntityGraph} information.
     *
     * @return
     */
    protected QueryHints getQueryHints() {
        return metadata == null ? QueryHints.NoHints.INSTANCE : DefaultQueryHints.of(entityInformation, metadata);
    }

    private AbstractJPAQuery<?, ?> doCreateQuery(QueryHints hints, @Nullable Predicate... predicate) {

        AbstractJPAQuery<?, ?> query = querydsl.createQuery(path);

        if (predicate != null) {
            query = query.where(predicate);
        }

        for (Map.Entry<String, Object> hint : hints) {
            query.setHint(hint.getKey(), hint.getValue());
        }

        return query;
    }

    /**
     * Executes the given {@link JPQLQuery} after applying the given {@link OrderSpecifier}s.
     *
     * @param query  must not be {@literal null}.
     * @param orders must not be {@literal null}.
     * @return
     */
    private <E> List<E> executeSorted(JPQLQuery<E> query, OrderSpecifier<?>... orders) {
        return executeSorted(query, new QSort(orders));
    }

    /**
     * Executes the given {@link JPQLQuery} after applying the given {@link Sort}.
     *
     * @param query must not be {@literal null}.
     * @param sort  must not be {@literal null}.
     * @return
     */
    private <E> List<E> executeSorted(JPQLQuery<E> query, Sort sort) {
        return querydsl.applySorting(sort, query).fetch();
    }
}
