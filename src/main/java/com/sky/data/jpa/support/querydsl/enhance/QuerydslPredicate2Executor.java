package com.sky.data.jpa.support.querydsl.enhance;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public interface QuerydslPredicate2Executor<T> {
    /**
     * Returns a single entity matching the given {@link Predicate} or {@link Optional#empty()} if none was found.
     *
     * @param predicate must not be {@literal null}.
     * @return a single entity matching the given {@link Predicate} or {@link Optional#empty()} if none was found.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the predicate yields more than one
     *                                                                        result.
     */
    <E> Optional<E> findOne(Predicate predicate, ConstructorExpression<E> ce);

    /**
     * Returns all entities matching the given {@link Predicate}. In case no match could be found an empty
     * {@link Iterable} is returned.
     *
     * @param predicate must not be {@literal null}.
     * @return all entities matching the given {@link Predicate}.
     */
    <E> Iterable<E> findAll(Predicate predicate, ConstructorExpression<E> ce);

    /**
     * Returns all entities matching the given {@link Predicate} applying the given {@link Sort}. In case no match could
     * be found an empty {@link Iterable} is returned.
     *
     * @param predicate must not be {@literal null}.
     * @param sort      the {@link Sort} specification to sort the results by, may be {@link Sort#empty()}, must not be
     *                  {@literal null}.
     * @return all entities matching the given {@link Predicate}.
     * @since 1.10
     */
    <E> Iterable<E> findAll(Predicate predicate, Sort sort, ConstructorExpression<E> ce);

    /**
     * Returns all entities matching the given {@link Predicate} applying the given {@link OrderSpecifier}s. In case no
     * match could be found an empty {@link Iterable} is returned.
     *
     * @param predicate must not be {@literal null}.
     * @param orders    the {@link OrderSpecifier}s to sort the results by.
     * @return all entities matching the given {@link Predicate} applying the given {@link OrderSpecifier}s.
     */
    <E> Iterable<E> findAll(Predicate predicate, ConstructorExpression<E> ce, OrderSpecifier<?>... orders);

    /**
     * Returns all entities ordered by the given {@link OrderSpecifier}s.
     *
     * @param orders the {@link OrderSpecifier}s to sort the results by.
     * @return all entities ordered by the given {@link OrderSpecifier}s.
     */
    <E> Iterable<E> findAll(ConstructorExpression<E> ce, OrderSpecifier<?>... orders);

    /**
     * Returns a {@link Page} of entities matching the given {@link Predicate}. In case no match could be found, an empty
     * {@link Page} is returned.
     *
     * @param predicate must not be {@literal null}.
     * @param pageable  may be {@link Pageable#unpaged()}, must not be {@literal null}.
     * @return a {@link Page} of entities matching the given {@link Predicate}.
     */
    <E> Page<E> findAll(Predicate predicate, Pageable pageable, ConstructorExpression<E> ce);
}
