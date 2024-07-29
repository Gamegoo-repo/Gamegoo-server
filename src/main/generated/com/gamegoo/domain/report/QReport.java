package com.gamegoo.domain.report;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = 1105800682L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReport report = new QReport("report");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath reportContent = createString("reportContent");

    public final com.gamegoo.domain.QMember reporter;

    public final ListPath<ReportTypeMapping, QReportTypeMapping> reportTypeMappingList = this.<ReportTypeMapping, QReportTypeMapping>createList("reportTypeMappingList", ReportTypeMapping.class, QReportTypeMapping.class, PathInits.DIRECT2);

    public final com.gamegoo.domain.QMember target;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReport(String variable) {
        this(Report.class, forVariable(variable), INITS);
    }

    public QReport(Path<? extends Report> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReport(PathMetadata metadata, PathInits inits) {
        this(Report.class, metadata, inits);
    }

    public QReport(Class<? extends Report> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reporter = inits.isInitialized("reporter") ? new com.gamegoo.domain.QMember(forProperty("reporter")) : null;
        this.target = inits.isInitialized("target") ? new com.gamegoo.domain.QMember(forProperty("target")) : null;
    }

}

