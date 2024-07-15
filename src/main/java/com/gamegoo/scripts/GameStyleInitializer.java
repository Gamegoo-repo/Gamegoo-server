package com.gamegoo.scripts;

//import com.gamegoo.domain.board.GameStyle2;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.repository.member.GameStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameStyleInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final GameStyleRepository gameStyleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isCreateMode(event)) {
            initializeGameStyles();
        }
    }

    private boolean isCreateMode(ApplicationReadyEvent event) {
        // jpa.hibernate.ddl-auto 값이 create인지 확인
        String ddlAuto = event.getApplicationContext().getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        return "create".equalsIgnoreCase(ddlAuto);
    }

    private void initializeGameStyles() {
        String[] styles = {
                "광물 탈출",
                "랭크 올리고 싶어요",
                "이기기만 하면 뭔들",
                "바른말 사용",
                "답장 빨라요",
                "마이크 필수",
                "마이크 안해요",
                "과도한 핑은 사절이에요",
                "즐겜러",
                "빡겜러",
                "원챔러",
                "욕하지 말아요",
                "뚝심있는 탑",
                "갱킹마스터 정글러",
                "1인군단 원딜러",
                "무한 백업 서포터",
                "칼바람 장인"
        };

        for (String style : styles) {
            GameStyle gameStyle = GameStyle.builder().styleName(style).build();
            gameStyleRepository.save(gameStyle);
        }

    }
}
