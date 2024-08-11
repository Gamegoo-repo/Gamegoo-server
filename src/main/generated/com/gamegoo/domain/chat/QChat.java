package com.gamegoo.domain.chat;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChat is a Querydsl query type for Chat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChat extends EntityPathBase<Chat> {

    private static final long serialVersionUID = 1643761898L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChat chat = new QChat("chat");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final QChatroom chatroom;

    public final StringPath contents = createString("contents");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.gamegoo.domain.member.QMember fromMember;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.gamegoo.domain.board.QBoard sourceBoard;

    public final NumberPath<Long> timestamp = createNumber("timestamp", Long.class);

    public final com.gamegoo.domain.member.QMember toMember;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChat(String variable) {
        this(Chat.class, forVariable(variable), INITS);
    }

    public QChat(Path<? extends Chat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChat(PathMetadata metadata, PathInits inits) {
        this(Chat.class, metadata, inits);
    }

    public QChat(Class<? extends Chat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatroom = inits.isInitialized("chatroom") ? new QChatroom(forProperty("chatroom"), inits.get("chatroom")) : null;
        this.fromMember = inits.isInitialized("fromMember") ? new com.gamegoo.domain.member.QMember(forProperty("fromMember")) : null;
        this.sourceBoard = inits.isInitialized("sourceBoard") ? new com.gamegoo.domain.board.QBoard(forProperty("sourceBoard"), inits.get("sourceBoard")) : null;
        this.toMember = inits.isInitialized("toMember") ? new com.gamegoo.domain.member.QMember(forProperty("toMember")) : null;
    }

}

