package uz.tenzorsoft.scaleapplication.service;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Service
@RequiredArgsConstructor
public class ControllerService {

    public void connect() throws Exception {
        address = InetAddress.getByName(CONTROLLER_IP);
        connection = new TCPMasterConnection(address);
        connection.setTimeout(3000);
        connection.setPort(port);
        connection.connect();
        isConnected = true;
        scalePort.openPort();
    }

    public void openGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(COIL_GREEN_LIGHT_1, true);
        writeCoil(COIL_OPEN_GATE_1, true);
        writeCoil(COIL_CLOSE_GATE_1, false);
    }

    public void openGate1(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        ScaleSystem.truckPosition = truckPosition;
        openGate1();
    }


    public void closeGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(COIL_GREEN_LIGHT_1, false);
        writeCoil(COIL_OPEN_GATE_1, false);
        writeCoil(COIL_CLOSE_GATE_1, true);
    }


    public void openGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(COIL_GREEN_LIGHT_2, true);
        writeCoil(COIL_OPEN_GATE_2, true);
        writeCoil(COIL_CLOSE_GATE_2, false);
    }

    public void openGate2(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        ScaleSystem.truckPosition = truckPosition;
        openGate2();
    }

    public void closeGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(COIL_GREEN_LIGHT_2, false);
        writeCoil(COIL_OPEN_GATE_2, false);
        writeCoil(COIL_CLOSE_GATE_2, true);
    }

    public boolean checkConnection(Integer coilAddress) throws ModbusException {
        if (isConnected) {
            return readCoil(coilAddress) == 1;
        }
        return false;
    }

    public boolean checkConnection(String ipAddress) throws IOException {
        InetAddress address = InetAddress.getByName(ipAddress);
        return address.isReachable(1500);
    }

    private void writeCoil(Integer coil, boolean state) throws ModbusException {
        WriteCoilRequest request = new WriteCoilRequest(coil, state);
        if (transaction == null) transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ModbusResponse response = transaction.getResponse();
        if (response instanceof ExceptionResponse) {
            throw new ModbusException("Modbus exception response");
        }
    }

    private int readCoil(Integer coilAddress) throws ModbusException {
        ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
        if (transaction == null) transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();
        ModbusResponse response = transaction.getResponse();

        if (response instanceof ExceptionResponse || response == null) {
            return -1;
        } else {
            ReadCoilsResponse readResponse = (ReadCoilsResponse) response;
            return readResponse.getCoils().getBit(0) ? 1 : 0;
        }
    }

    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.close();
            isConnected = false;
            scalePort.closePort();
        }
    }
}
