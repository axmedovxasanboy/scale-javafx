package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;
import uz.tenzorsoft.scaleapplication.domain.request.UserRequest;
import uz.tenzorsoft.scaleapplication.domain.response.UserResponse;
import uz.tenzorsoft.scaleapplication.repository.UserRepository;

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
}

