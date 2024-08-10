package com.gamegoo.domain.chat;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberChatroom is a Querydsl query type for MemberChatroom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberChatroom extends EntityPathBase<MemberChatroom> {

    private static final long serialVersionUID = -1788261377L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberChatroom memberChatroom = new QMemberChatroom("memberChatroom");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final QChatroom chatroom;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastJoinDate = createDateTime("lastJoinDate", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> lastViewDate = createDateTime("lastViewDate", java.time.LocalDateTime.class);

    public final com.gamegoo.domain.member.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberChatroom(String variable) {
        this(MemberChatroom.class, forVariable(variable), INITS);
    }

    public QMemberChatroom(Path<? extends MemberChatroom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberChatroom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberChatroom(PathMetadata metadata, PathInits inits) {
        this(MemberChatroom.class, metadata, inits);
    }

    public QMemberChatroom(Class<? extends MemberChatroom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatroom = inits.isInitialized("chatroom") ? new QChatroom(forProperty("chatroom"), inits.get("chatroom")) : null;
        this.member = inits.isInitialized("member") ? new com.gamegoo.domain.member.QMember(forProperty("member")) : null;
    }

}

