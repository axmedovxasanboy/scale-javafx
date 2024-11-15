package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.repository.LogRepository;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public LogEntity save(LogEntity logEntity) {
        return logRepository.save(logEntity);
    }
}
