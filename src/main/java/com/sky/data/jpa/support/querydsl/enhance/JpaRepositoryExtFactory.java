package com.sky.data.jpa.support.querydsl.enhance;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

import javax.persistence.EntityManager;
import java.io.Serializable;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

public class JpaRepositoryExtFactory extends JpaRepositoryFactory {
    private final EntityManager entityManager;
    private final CrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;

    private EntityPathResolver entityPathResolver;

    public JpaRepositoryExtFactory(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
        this.crudMethodMetadataPostProcessor = new CrudMethodMetadataPostProcessor();
        this.entityPathResolver = SimpleEntityPathResolver.INSTANCE;
        addRepositoryProxyPostProcessor(crudMethodMetadataPostProcessor);
    }

    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {
        RepositoryComposition.RepositoryFragments fragments = super.getRepositoryFragments(metadata);
        boolean isQueryDslRepository = QUERY_DSL_PRESENT
            && QuerydslPredicate2Executor.class.isAssignableFrom(metadata.getRepositoryInterface());
        if (isQueryDslRepository) {
            if (metadata.isReactiveRepository()) {
                throw new InvalidDataAccessApiUsageException(
                    "Cannot combine Querydsl and reactive repository support in a single interface");
            }

            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());

            Object querydslFragment = getTargetRepositoryViaReflection(
                QuerydslJpaPredicate2Executor.class,
                entityInformation,
                entityManager,
                entityPathResolver,
                crudMethodMetadataPostProcessor.getCrudMethodMetadata());

            fragments = fragments.append(RepositoryFragment.implemented(querydslFragment));
        }
        return fragments;
    }
}
