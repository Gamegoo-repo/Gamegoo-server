package com.gamegoo.domain.champion;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChampion is a Querydsl query type for Champion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChampion extends EntityPathBase<Champion> {

    private static final long serialVersionUID = 927182090L;

    public static final QChampion champion = new QChampion("champion");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QChampion(String variable) {
        super(Champion.class, forVariable(variable));
    }

    public QChampion(Path<? extends Champion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChampion(PathMetadata metadata) {
        super(Champion.class, metadata);
    }

}

