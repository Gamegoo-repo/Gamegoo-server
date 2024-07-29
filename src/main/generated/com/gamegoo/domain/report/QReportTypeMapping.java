package com.gamegoo.domain.report;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReportTypeMapping is a Querydsl query type for ReportTypeMapping
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportTypeMapping extends EntityPathBase<ReportTypeMapping> {

    private static final long serialVersionUID = -1516834838L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReportTypeMapping reportTypeMapping = new QReportTypeMapping("reportTypeMapping");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QReport report;

    public final QReportType reportType;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReportTypeMapping(String variable) {
        this(ReportTypeMapping.class, forVariable(variable), INITS);
    }

    public QReportTypeMapping(Path<? extends ReportTypeMapping> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReportTypeMapping(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReportTypeMapping(PathMetadata metadata, PathInits inits) {
        this(ReportTypeMapping.class, metadata, inits);
    }

    public QReportTypeMapping(Class<? extends ReportTypeMapping> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.report = inits.isInitialized("report") ? new QReport(forProperty("report"), inits.get("report")) : null;
        this.reportType = inits.isInitialized("reportType") ? new QReportType(forProperty("reportType")) : null;
    }

}

