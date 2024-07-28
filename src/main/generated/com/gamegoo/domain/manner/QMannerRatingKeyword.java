package com.gamegoo.domain.manner;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMannerRatingKeyword is a Querydsl query type for MannerRatingKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMannerRatingKeyword extends EntityPathBase<MannerRatingKeyword> {

    private static final long serialVersionUID = -939072414L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMannerRatingKeyword mannerRatingKeyword = new QMannerRatingKeyword("mannerRatingKeyword");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMannerKeyword mannerKeyword;

    public final QMannerRating mannerRating;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMannerRatingKeyword(String variable) {
        this(MannerRatingKeyword.class, forVariable(variable), INITS);
    }

    public QMannerRatingKeyword(Path<? extends MannerRatingKeyword> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMannerRatingKeyword(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMannerRatingKeyword(PathMetadata metadata, PathInits inits) {
        this(MannerRatingKeyword.class, metadata, inits);
    }

    public QMannerRatingKeyword(Class<? extends MannerRatingKeyword> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mannerKeyword = inits.isInitialized("mannerKeyword") ? new QMannerKeyword(forProperty("mannerKeyword")) : null;
        this.mannerRating = inits.isInitialized("mannerRating") ? new QMannerRating(forProperty("mannerRating"), inits.get("mannerRating")) : null;
    }

}

