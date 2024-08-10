package com.gamegoo.domain.friend;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFriendRequests is a Querydsl query type for FriendRequests
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFriendRequests extends EntityPathBase<FriendRequests> {

    private static final long serialVersionUID = -1336304498L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFriendRequests friendRequests = new QFriendRequests("friendRequests");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.gamegoo.domain.member.QMember fromMember;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<FriendRequestStatus> status = createEnum("status", FriendRequestStatus.class);

    public final com.gamegoo.domain.member.QMember toMember;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFriendRequests(String variable) {
        this(FriendRequests.class, forVariable(variable), INITS);
    }

    public QFriendRequests(Path<? extends FriendRequests> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFriendRequests(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFriendRequests(PathMetadata metadata, PathInits inits) {
        this(FriendRequests.class, metadata, inits);
    }

    public QFriendRequests(Class<? extends FriendRequests> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.fromMember = inits.isInitialized("fromMember") ? new com.gamegoo.domain.member.QMember(forProperty("fromMember")) : null;
        this.toMember = inits.isInitialized("toMember") ? new com.gamegoo.domain.member.QMember(forProperty("toMember")) : null;
    }

}

