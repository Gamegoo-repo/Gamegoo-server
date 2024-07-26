package com.gamegoo.scripts;

import com.gamegoo.domain.report.ReportType;
import com.gamegoo.repository.report.ReportTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportTypeInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ReportTypeRepository reportTypeRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isCreateMode(event)) {
            initializeReportTypes();
        }
    }
    private boolean isCreateMode(ApplicationReadyEvent event) {
        // jpa.hibernate.ddl-auto 값이 create인지 확인
        String ddlAuto = event.getApplicationContext().getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        return "create".equalsIgnoreCase(ddlAuto);
    }

    private void initializeReportTypes() {
        String[] types = {
                "스팸 홍보/도배글",
                "불법 정보 포함",
                "성희롱 발언",
                "욕설/ 혐오/ 차별적 표현",
                "개인 정보 노출",
                "불쾌한 표현"
        };

        for (String type : types) {
            ReportType reportType = ReportType.builder().reportTypeContent(type).build();
            reportTypeRepository.save(reportType);
        }
    }
}
