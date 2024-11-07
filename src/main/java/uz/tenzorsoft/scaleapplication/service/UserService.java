package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;
import uz.tenzorsoft.scaleapplication.domain.request.UserRequest;
import uz.tenzorsoft.scaleapplication.domain.response.UserResponse;
import uz.tenzorsoft.scaleapplication.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements BaseService<UserEntity, UserResponse, UserRequest> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserResponse validateUser(String phoneNumber, String password) {
        UserEntity user = userRepository.findByPhoneNumberAndPassword(phoneNumber, password)
                .orElse(null);
        if (user == null) {
            return null;
        }
        Instances.currentUser = user;
        return entityToResponse(user);
    }

    public List<UserResponse> getNotSentData() {
        List<UserResponse> result = new ArrayList<>();
        List<UserEntity> notSentData = userRepository.findByIsSent(false);
        for (UserEntity user : notSentData) {
            UserResponse response = new UserResponse(
                    user.getUsername(), user.getPassword(), user.getPhoneNumber()
            );
            response.setId(user.getId());
            response.setCreatedAt(user.getCreatedAt());
            response.setIdOnServer(user.getIdOnServer());
            result.add(response);
        }
        return result;
    }

    public void dataSent(List<UserResponse> notSentData, Map<Long, Long> userMap) {
        if (userMap == null || userMap.isEmpty()) {
            return;
        }
        notSentData.forEach(user -> {
            UserEntity entity = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException(user.getId() + " is not found from database"));
            entity.setIsSent(true);
            entity.setIdOnServer(userMap.get(entity.getId()));
            userRepository.save(entity);
        });

    }

    @Override
    public UserResponse entityToResponse(UserEntity entity) {
        return new UserResponse(entity.getUsername(), entity.getPassword(), entity.getPhoneNumber());
    }

    @Override
    public UserEntity requestToEntity(UserRequest request) {
        return null;
    }

    public UserResponse save(UserEntity userEntity) {
        return entityToResponse(userRepository.save(userEntity));
    }

    public UserEntity findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }
}

