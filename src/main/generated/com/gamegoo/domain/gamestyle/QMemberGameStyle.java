package com.gamegoo.domain.gamestyle;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberGameStyle is a Querydsl query type for MemberGameStyle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberGameStyle extends EntityPathBase<MemberGameStyle> {

    private static final long serialVersionUID = 1435283698L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberGameStyle memberGameStyle = new QMemberGameStyle("memberGameStyle");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QGameStyle gameStyle;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.gamegoo.domain.Member.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberGameStyle(String variable) {
        this(MemberGameStyle.class, forVariable(variable), INITS);
    }

    public QMemberGameStyle(Path<? extends MemberGameStyle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberGameStyle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberGameStyle(PathMetadata metadata, PathInits inits) {
        this(MemberGameStyle.class, metadata, inits);
    }

    public QMemberGameStyle(Class<? extends MemberGameStyle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gameStyle = inits.isInitialized("gameStyle") ? new QGameStyle(forProperty("gameStyle")) : null;
        this.member = inits.isInitialized("member") ? new com.gamegoo.domain.Member.QMember(forProperty("member")) : null;
    }

}

