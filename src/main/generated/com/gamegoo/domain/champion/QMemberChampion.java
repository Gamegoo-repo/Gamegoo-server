package com.gamegoo.domain.champion;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberChampion is a Querydsl query type for MemberChampion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberChampion extends EntityPathBase<MemberChampion> {

    private static final long serialVersionUID = 1754178820L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberChampion memberChampion = new QMemberChampion("memberChampion");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final QChampion champion;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.gamegoo.domain.member.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberChampion(String variable) {
        this(MemberChampion.class, forVariable(variable), INITS);
    }

    public QMemberChampion(Path<? extends MemberChampion> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberChampion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberChampion(PathMetadata metadata, PathInits inits) {
        this(MemberChampion.class, metadata, inits);
    }

    public QMemberChampion(Class<? extends MemberChampion> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.champion = inits.isInitialized("champion") ? new QChampion(forProperty("champion")) : null;
        this.member = inits.isInitialized("member") ? new com.gamegoo.domain.member.QMember(forProperty("member")) : null;
    }

}

