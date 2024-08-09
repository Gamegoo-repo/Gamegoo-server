package com.gamegoo.domain.manner;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMannerRating is a Querydsl query type for MannerRating
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMannerRating extends EntityPathBase<MannerRating> {

    private static final long serialVersionUID = 1057037191L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMannerRating mannerRating = new QMannerRating("mannerRating");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.gamegoo.domain.member.QMember fromMember;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPositive = createBoolean("isPositive");

    public final ListPath<MannerRatingKeyword, QMannerRatingKeyword> mannerRatingKeywordList = this.<MannerRatingKeyword, QMannerRatingKeyword>createList("mannerRatingKeywordList", MannerRatingKeyword.class, QMannerRatingKeyword.class, PathInits.DIRECT2);

    public final com.gamegoo.domain.member.QMember toMember;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMannerRating(String variable) {
        this(MannerRating.class, forVariable(variable), INITS);
    }

    public QMannerRating(Path<? extends MannerRating> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMannerRating(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMannerRating(PathMetadata metadata, PathInits inits) {
        this(MannerRating.class, metadata, inits);
    }

    public QMannerRating(Class<? extends MannerRating> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.fromMember = inits.isInitialized("fromMember") ? new com.gamegoo.domain.member.QMember(forProperty("fromMember")) : null;
        this.toMember = inits.isInitialized("toMember") ? new com.gamegoo.domain.member.QMember(forProperty("toMember")) : null;
    }

}

