package com.gamegoo.domain.notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationType is a Querydsl query type for NotificationType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationType extends EntityPathBase<NotificationType> {

    private static final long serialVersionUID = 817018916L;

    public static final QNotificationType notificationType = new QNotificationType("notificationType");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imgUrl = createString("imgUrl");

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QNotificationType(String variable) {
        super(NotificationType.class, forVariable(variable));
    }

    public QNotificationType(Path<? extends NotificationType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationType(PathMetadata metadata) {
        super(NotificationType.class, metadata);
    }

}

