package com.gamegoo.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardGameStyle is a Querydsl query type for BoardGameStyle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardGameStyle extends EntityPathBase<BoardGameStyle> {

    private static final long serialVersionUID = -492166491L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardGameStyle boardGameStyle = new QBoardGameStyle("boardGameStyle");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final QBoard board;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.gamegoo.domain.gamestyle.QGameStyle gameStyle;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBoardGameStyle(String variable) {
        this(BoardGameStyle.class, forVariable(variable), INITS);
    }

    public QBoardGameStyle(Path<? extends BoardGameStyle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardGameStyle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardGameStyle(PathMetadata metadata, PathInits inits) {
        this(BoardGameStyle.class, metadata, inits);
    }

    public QBoardGameStyle(Class<? extends BoardGameStyle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new QBoard(forProperty("board"), inits.get("board")) : null;
        this.gameStyle = inits.isInitialized("gameStyle") ? new com.gamegoo.domain.gamestyle.QGameStyle(forProperty("gameStyle")) : null;
    }

}

