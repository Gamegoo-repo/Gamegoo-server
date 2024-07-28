package com.gamegoo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmailVerifyRecord is a Querydsl query type for EmailVerifyRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailVerifyRecord extends EntityPathBase<EmailVerifyRecord> {

    private static final long serialVersionUID = 545433698L;

    public static final QEmailVerifyRecord emailVerifyRecord = new QEmailVerifyRecord("emailVerifyRecord");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEmailVerifyRecord(String variable) {
        super(EmailVerifyRecord.class, forVariable(variable));
    }

    public QEmailVerifyRecord(Path<? extends EmailVerifyRecord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmailVerifyRecord(PathMetadata metadata) {
        super(EmailVerifyRecord.class, metadata);
    }

}

