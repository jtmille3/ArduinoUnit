package name.miller.arduinounit;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ArduinoUnitPreferences extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String ARDUINO_UNIT_PORT = "ArduinoUnitPort";
	private List portList;

	@Override
	protected Control createContents(final Composite parent) {
		Composite entryTable = new Composite(parent, SWT.NULL);

		//Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		final Label portLabel = new Label(entryTable, SWT.NONE);
		portLabel.setText("Arduino Ports");

		portList = new List(entryTable, SWT.SINGLE);
		portList.setItems(SerialCommunicator.getPortNames());
		final String defaultValue = getPort();
		for (int i = 0; i < portList.getItemCount(); i++) {
			final String port = portList.getItem(i);
			if (port.equals(defaultValue)) {
				portList.select(i);
				break;
			}
		}

		//Create a data that takes up the extra space in the dialog and spans both columns.
		data = new GridData(GridData.FILL_BOTH);
		portList.setLayoutData(data);

		return entryTable;
	}

	@Override
	public String getDescription() {
		return "ArduinoUnit communicates between Eclipse JUnit runner and an Arduino.  To do so successfully you must "
				+ "select a valid communication port to connect to.";
	}

	@Override
	public String getTitle() {
		return "ArduinoUnit";
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(ArduinoUnitActivator.getDefault().getPreferenceStore());
	}

	@Override
	public boolean performOk() {
		if (portList.getSelectionCount() > 0) {
			final String port = portList.getSelection()[0];
			ArduinoUnitActivator.getDefault().getPreferenceStore().putValue(ARDUINO_UNIT_PORT, port);
		}
		return super.performOk();
	}

	public static String getPort() {
		return ArduinoUnitActivator.getDefault().getPreferenceStore().getString(ARDUINO_UNIT_PORT);
	}

}
