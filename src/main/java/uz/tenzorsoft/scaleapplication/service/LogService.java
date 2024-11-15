package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.repository.LogRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public LogEntity save(LogEntity logEntity) {
        return logRepository.save(logEntity);
    }

    public List<LogEntity> getNotSentLogs() {
        return logRepository.findByIdOnServer(null);
    }

    public void dataSent(List<LogEntity> notSentLogs, Map<Long, Long> logMap) {
        if (logMap == null || logMap.isEmpty()) {
            return;
        }
        notSentLogs.forEach(log -> {
            log.setIsSentToCloud(true);
            log.setIdOnServer(logMap.get(log.getId()));
            logRepository.save(log);
        });
    }
}
