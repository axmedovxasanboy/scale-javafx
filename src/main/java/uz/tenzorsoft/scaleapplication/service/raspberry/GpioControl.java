package uz.tenzorsoft.scaleapplication.service.raspberry;

import com.pi4j.Pi4J;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalOutputProvider;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalInputProvider;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.platform.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.PinState;
import uz.tenzorsoft.scaleapplication.service.LogService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.RASP_SENSOR_1;

@Service
@RequiredArgsConstructor
public class GpioControl {
    private final LogService logService;

    private Context pi4jOut;
    private Context pi4jIn;

    public void initialize() {
        if (pi4jOut == null) {
            pi4jOut = Pi4J.newContextBuilder()
                    .add(GpioDDigitalOutputProvider.newInstance())
                    .build();
        }

        if (pi4jIn == null) {
            pi4jIn = Pi4J.newContextBuilder()
                    .add(GpioDDigitalInputProvider.newInstance())
                    .build();
        }

        Platform platform = pi4jOut.platform();
        System.out.println("Platform: " + (platform != null ? platform.name() : "Not Initialized"));

        controlPinsInitialization(pi4jOut);
        statusPinsInitialization(pi4jIn);
        setStatusListeners();
    }

    private void setStatusListeners() {
        Map<Integer, Consumer<Boolean>> pinToSensorMap = Map.of(
                17, (status) -> sensor1Connection = status,
                22, (status) -> sensor2Connection = status,
                27, (status) -> sensor3Connection = status
        );

        for (int pin : STATUS_PINS) {
            DigitalInput input = inputPins.get(pin);
            if (input != null && pinToSensorMap.containsKey(pin)) {
                Consumer<Boolean> sensorUpdater = pinToSensorMap.get(pin);
                input.addListener((event) -> {
                    boolean isHigh = event.state().isHigh();
                    sensorUpdater.accept(isHigh);
                });
            }
        }
    }

    public boolean controlPin(int pin, PinState state) {
        System.out.println("pin = " + pin + " -> " + state);
        if (!outputPins.containsKey(pin)) {
            throw new RuntimeException("Pin number not found " + pin);
        }
        DigitalOutput output = outputPins.get(pin);
        if (state == PinState.HIGH) {
            output.high();
            return true;
        } else if (state == PinState.LOW) {
            output.low();
            return true;
        }
        return false;
    }

    private void statusPinsInitialization(Context pi4jIn) {
        for (int pinAddress : STATUS_PINS) {
            try {
                DigitalInputConfigBuilder config = DigitalInput.newConfigBuilder(pi4jIn)
                        .id("pin-" + pinAddress)
                        .name("Status Pin " + pinAddress)
                        .address(pinAddress)
                        .pull(PullResistance.PULL_DOWN);
                inputPins.put(pinAddress, pi4jIn.create(config));
                isAvailableToConnect = true;
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    private void controlPinsInitialization(Context pi4jOut) {
        for (int pinAddress : CONTROL_PINS) {
            try {
                DigitalOutputConfigBuilder config = DigitalOutput.newConfigBuilder(pi4jOut)
                        .id("pin-" + pinAddress)
                        .name("Control Pin " + pinAddress)
                        .address(pinAddress)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW);
                outputPins.put(pinAddress, pi4jOut.create(config));
                isAvailableToConnect = true;
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    public void shutdown() {
        sensor1Connection = false;
        sensor2Connection = false;
        sensor3Connection = false;
        gate1Connection = false;
        gate2Connection = false;
//        if (pi4jOut != null) pi4jOut.shutdown();
//        if (pi4jIn != null) pi4jIn.shutdown();
    }

    public void shutdownCompletely() {
        if (pi4jOut != null) pi4jOut.shutdown();
        if (pi4jIn != null) pi4jIn.shutdown();
    }

    public void getSensorStatuses() {
        for (int pin : STATUS_PINS) {
            DigitalInput input = inputPins.get(pin);
            if (input != null) {
                switch (pin) {
                    case 17 -> sensor1Connection = input.isHigh();
                    case 22 -> sensor2Connection = input.isHigh();
                    case 27 -> sensor3Connection = input.isHigh();
                }
            }
        }
    }
}

