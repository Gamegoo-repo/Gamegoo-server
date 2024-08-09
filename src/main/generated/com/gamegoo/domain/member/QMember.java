package com.gamegoo.domain.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 321613610L;

    public static final QMember member = new QMember("member1");

    public final com.gamegoo.domain.common.QBaseDateTimeEntity _super = new com.gamegoo.domain.common.QBaseDateTimeEntity(this);

    public final BooleanPath blind = createBoolean("blind");

    public final ListPath<com.gamegoo.domain.Block, com.gamegoo.domain.QBlock> blockList = this.<com.gamegoo.domain.Block, com.gamegoo.domain.QBlock>createList("blockList", com.gamegoo.domain.Block.class, com.gamegoo.domain.QBlock.class, PathInits.DIRECT2);

    public final ListPath<com.gamegoo.domain.board.Board, com.gamegoo.domain.board.QBoard> boardList = this.<com.gamegoo.domain.board.Board, com.gamegoo.domain.board.QBoard>createList("boardList", com.gamegoo.domain.board.Board.class, com.gamegoo.domain.board.QBoard.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final StringPath gameName = createString("gameName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<LoginType> loginType = createEnum("loginType", LoginType.class);

    public final NumberPath<Integer> mainPosition = createNumber("mainPosition", Integer.class);

    public final NumberPath<Integer> mannerLevel = createNumber("mannerLevel", Integer.class);

    public final ListPath<com.gamegoo.domain.manner.MannerRating, com.gamegoo.domain.manner.QMannerRating> mannerRatingList = this.<com.gamegoo.domain.manner.MannerRating, com.gamegoo.domain.manner.QMannerRating>createList("mannerRatingList", com.gamegoo.domain.manner.MannerRating.class, com.gamegoo.domain.manner.QMannerRating.class, PathInits.DIRECT2);

    public final ListPath<com.gamegoo.domain.champion.MemberChampion, com.gamegoo.domain.champion.QMemberChampion> memberChampionList = this.<com.gamegoo.domain.champion.MemberChampion, com.gamegoo.domain.champion.QMemberChampion>createList("memberChampionList", com.gamegoo.domain.champion.MemberChampion.class, com.gamegoo.domain.champion.QMemberChampion.class, PathInits.DIRECT2);

    public final ListPath<com.gamegoo.domain.chat.MemberChatroom, com.gamegoo.domain.chat.QMemberChatroom> memberChatroomList = this.<com.gamegoo.domain.chat.MemberChatroom, com.gamegoo.domain.chat.QMemberChatroom>createList("memberChatroomList", com.gamegoo.domain.chat.MemberChatroom.class, com.gamegoo.domain.chat.QMemberChatroom.class, PathInits.DIRECT2);

    public final ListPath<com.gamegoo.domain.gamestyle.MemberGameStyle, com.gamegoo.domain.gamestyle.QMemberGameStyle> memberGameStyleList = this.<com.gamegoo.domain.gamestyle.MemberGameStyle, com.gamegoo.domain.gamestyle.QMemberGameStyle>createList("memberGameStyleList", com.gamegoo.domain.gamestyle.MemberGameStyle.class, com.gamegoo.domain.gamestyle.QMemberGameStyle.class, PathInits.DIRECT2);

    public final BooleanPath mike = createBoolean("mike");

    public final ListPath<com.gamegoo.domain.notification.Notification, com.gamegoo.domain.notification.QNotification> notificationList = this.<com.gamegoo.domain.notification.Notification, com.gamegoo.domain.notification.QNotification>createList("notificationList", com.gamegoo.domain.notification.Notification.class, com.gamegoo.domain.notification.QNotification.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final NumberPath<Integer> profileImage = createNumber("profileImage", Integer.class);

    public final StringPath rank = createString("rank");

    public final StringPath refreshToken = createString("refreshToken");

    public final ListPath<com.gamegoo.domain.report.Report, com.gamegoo.domain.report.QReport> reportList = this.<com.gamegoo.domain.report.Report, com.gamegoo.domain.report.QReport>createList("reportList", com.gamegoo.domain.report.Report.class, com.gamegoo.domain.report.QReport.class, PathInits.DIRECT2);

    public final NumberPath<Integer> subPosition = createNumber("subPosition", Integer.class);

    public final StringPath tag = createString("tag");

    public final EnumPath<Tier> tier = createEnum("tier", Tier.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Double> winRate = createNumber("winRate", Double.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

