package com.gamegoo.domain.gamestyle;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGameStyle is a Querydsl query type for GameStyle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGameStyle extends EntityPathBase<GameStyle> {

    private static final long serialVersionUID = -851035092L;

    public static final QGameStyle gameStyle = new QGameStyle("gameStyle");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath styleName = createString("styleName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGameStyle(String variable) {
        super(GameStyle.class, forVariable(variable));
    }

    public QGameStyle(Path<? extends GameStyle> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGameStyle(PathMetadata metadata) {
        super(GameStyle.class, metadata);
    }

}

