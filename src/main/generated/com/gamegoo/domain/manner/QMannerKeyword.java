package com.gamegoo.domain.manner;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMannerKeyword is a Querydsl query type for MannerKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMannerKeyword extends EntityPathBase<MannerKeyword> {

    private static final long serialVersionUID = 905376063L;

    public static final QMannerKeyword mannerKeyword = new QMannerKeyword("mannerKeyword");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final StringPath contents = createString("contents");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPositive = createBoolean("isPositive");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMannerKeyword(String variable) {
        super(MannerKeyword.class, forVariable(variable));
    }

    public QMannerKeyword(Path<? extends MannerKeyword> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMannerKeyword(PathMetadata metadata) {
        super(MannerKeyword.class, metadata);
    }

}

