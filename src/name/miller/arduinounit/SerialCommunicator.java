package name.miller.arduinounit;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SerialCommunicator implements SerialPortEventListener {

	private SerialPort serialPort;

	private BufferedReader input;
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 115200;

	public static String[] getPortNames() {
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		List<String> list = new ArrayList<>();
		while (ports.hasMoreElements()) {
			final CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			list.add(port.getName());
		}

		return list.toArray(new String[] {});
	}

	public SerialCommunicator() throws Exception {

	}

	public void open(final String port) throws Exception {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

			if (currPortId.getName().equals(port)) {
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		if (portId.isCurrentlyOwned()) {
			System.out.println("Port ID is currently owned.");
			return;
		}

		// open serial port, and use class name for the appName.
		serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

		// set port parameters
		serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		// open the streams
		input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
		output = serialPort.getOutputStream();

		// add event listeners
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);

		Thread.sleep(3000); // allow time for protocol to establish...
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public synchronized void write(final String message) throws Exception {
		System.out.println(message);
		output.write(message.getBytes());
		output.flush();

		wait(); // block until we get ack
	}

	public synchronized void write(final Integer message) throws Exception {
		write("" + message); // why?  Because we set this up to go in chunks of 8 bytes
	}

	@Override
	public synchronized void serialEvent(final SerialPortEvent oEvent) {
		switch (oEvent.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				final String value = input.readLine();
				System.out.println(value);
				if ("6".equals(value)) {
					notifyAll();
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
			break;
		case SerialPortEvent.CD:
			System.out.println("Carrier Detected");
			break;
		case SerialPortEvent.BI:
			System.out.println("Break Interrupt");
			break;
		case SerialPortEvent.CTS:
			System.out.println("Clear to Send");
			break;
		case SerialPortEvent.DSR:
			System.out.println("Data Set Ready");
			break;
		case SerialPortEvent.RI:
			System.out.println("Ring Indicator");
			break;
		default:
			System.out.println("Error detected");
			break;
		}
	}

	/*
	 * Test our protocol
	 */
	public static void main(final String[] args) throws Exception {
		// great success
		SerialCommunicator comm = new SerialCommunicator();
		comm.open("/dev/tty.usbmodem1421");
		comm.write("-1"); // open
		final int tests = 2;
		comm.write(tests); // count
		for (int i = 0; i < tests; i++) {
			comm.write(-4); // success
		}
		comm.write("-2"); // close
		comm.close();
	}
}
