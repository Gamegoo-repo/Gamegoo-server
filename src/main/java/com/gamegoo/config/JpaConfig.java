package com.gamegoo.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        //쿼리를 작성하는 JPAQueryFactory에 EntityManager를 넘겨 사용한다.
        return new JPAQueryFactory(em);
    }
}
