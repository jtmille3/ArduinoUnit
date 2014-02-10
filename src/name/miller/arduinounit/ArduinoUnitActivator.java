package name.miller.arduinounit;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ArduinoUnitActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ArduinoUnit"; //$NON-NLS-1$

	// The shared instance
	private static ArduinoUnitActivator plugin;

	/**
	 * The constructor
	 */
	public ArduinoUnitActivator() {
		setDebugging(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ArduinoUnitActivator getDefault() {
		return plugin;
	}

}
