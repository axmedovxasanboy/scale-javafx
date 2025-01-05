package uz.tenzorsoft.scaleapplication.service.raspberry;

import com.ghgande.j2mod.modbus.ModbusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.enumerators.PinState;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Service
@RequiredArgsConstructor
public class RaspberryService {

    private final GpioControl gpioControl;

    public boolean openGate1() throws RuntimeException, InterruptedException {
        if (!isConnected) {
            throw new RuntimeException("Not connected to controller");
        }
        sendCommand(RASP_GREEN_LIGHT_1, PinState.HIGH);
        sendCommand(RASP_OPEN_GATE_1, PinState.HIGH);
        Thread.sleep(500);
        sendCommand(RASP_OPEN_GATE_1, PinState.LOW);
        return true;
    }

    private void sendCommand(Integer pinNumber, PinState state) {
        gpioControl.controlPin(pinNumber, state);
    }

}
