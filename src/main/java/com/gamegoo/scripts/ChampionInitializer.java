package com.gamegoo.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.domain.champion.Champion;
import com.gamegoo.repository.member.ChampionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ChampionInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final ChampionRepository championRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isCreateMode(event)) {
            try {
                initializeChampions();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isCreateMode(ApplicationReadyEvent event) {
        // jpa.hibernate.ddl-auto 값이 create인지 확인
        String ddlAuto = event.getApplicationContext().getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        return "create".equalsIgnoreCase(ddlAuto);
    }

    private void initializeChampions() throws IOException {
        // JSON 파일을 읽어 파싱합니다.
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("static/champion.json").getInputStream();
        JsonNode rootNode = mapper.readTree(inputStream);
        JsonNode dataNode = rootNode.path("data");

        for (JsonNode championNode : dataNode) {
            Long key = championNode.path("key").asLong();
            String name = championNode.path("name").asText();

            Champion champion = new Champion();
            champion.setId(key);
            champion.setName(name);

            championRepository.save(champion);
        }
    }
}
