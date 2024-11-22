package uz.tenzorsoft.scaleapplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;
import uz.tenzorsoft.scaleapplication.domain.request.CommandsRequest;
import uz.tenzorsoft.scaleapplication.repository.CommandsRepository;

import java.util.Optional;

// CommansServiceImpl.java
@Service
public class CommandsService {

    @Autowired
    private CommandsRepository commandsRepository;

    public CommandsEntity saveOrUpdateCommands(CommandsRequest newCommand) {
        // Find by scaleId in the database (CommandsEntity type)
        Optional<CommandsEntity> existingCommand = commandsRepository.findByScaleId(newCommand.getScaleId());

        if (existingCommand.isPresent()) {
            // If an existing command is found, update it
            CommandsEntity commandToUpdate = existingCommand.get();
            commandToUpdate.setOpenGate1(newCommand.getOpenGate1());
            commandToUpdate.setCloseGate1(newCommand.getCloseGate1());
            commandToUpdate.setWeighing(newCommand.getWeighing());
            commandToUpdate.setOpenGate2(newCommand.getOpenGate2());
            commandToUpdate.setCloseGate2(newCommand.getCloseGate2());
            // Save the updated entity
            commandsRepository.save(commandToUpdate);
            return commandToUpdate;
        } else {
            // If not found, create a new CommandsEntity and save it
            CommandsEntity newEntity = new CommandsEntity();
            newEntity.setScaleId(newCommand.getScaleId());
            newEntity.setOpenGate1(newCommand.getOpenGate1());
            newEntity.setCloseGate1(newCommand.getCloseGate1());
            newEntity.setWeighing(newCommand.getWeighing());
            newEntity.setOpenGate2(newCommand.getOpenGate2());
            newEntity.setCloseGate2(newCommand.getCloseGate2());
            // Save the new entity
            commandsRepository.save(newEntity);
            return newEntity;
        }
    }

}

