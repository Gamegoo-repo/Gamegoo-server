package com.gamegoo.domain.report;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReportType is a Querydsl query type for ReportType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportType extends EntityPathBase<ReportType> {

    private static final long serialVersionUID = -1399575484L;

    public static final QReportType reportType = new QReportType("reportType");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath reportTypeContent = createString("reportTypeContent");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReportType(String variable) {
        super(ReportType.class, forVariable(variable));
    }

    public QReportType(Path<? extends ReportType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReportType(PathMetadata metadata) {
        super(ReportType.class, metadata);
    }

}

