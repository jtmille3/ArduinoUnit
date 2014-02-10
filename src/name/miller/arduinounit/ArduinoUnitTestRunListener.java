package name.miller.arduinounit;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestElementContainer;
import org.eclipse.jdt.junit.model.ITestRunSession;

public class ArduinoUnitTestRunListener extends TestRunListener {

	private int failures = 0;
	private final SerialCommunicator comm;
	private boolean enabled;

	private long time = 0L;

	public ArduinoUnitTestRunListener() throws Exception {
		this.comm = new SerialCommunicator();
		System.setOut(new PrintStream(new File("EclipseJUnitRunner.out.log")));
		System.setErr(new PrintStream(new File("EclipseJUnitRunner.error.log")));
	}

	private static final String DEBUG_ONE = "arduinojunit/debug/option1";

	public void println(final String message) {
		final String debugOption = Platform.getDebugOption(DEBUG_ONE);
		if (name.miller.arduinounit.ArduinoUnitActivator.getDefault().isDebugging() && "true".equalsIgnoreCase(debugOption))
			System.out.println(message);
	}

	private void printResults(final ITestRunSession session) {
		final int count = getTestCount(session.getChildren());
		println("Ran " + count + " with " + failures + " failures.");
	}

	private int getTestCount(final ITestElement[] elements) {
		int count = 0;
		for (final ITestElement element : elements) {
			count += getTestCount(element);
		}

		return count;
	}

	public int getTestCount(final ITestElement element) {
		int count = 0;
		if (element instanceof ITestElementContainer) { // suite
			ITestElementContainer container = (ITestElementContainer) element;
			count += getTestCount(container.getChildren());
		} else {
			count += 1;
		}

		return count;
	}

	@Override
	public void sessionLaunched(final ITestRunSession session) {
		super.sessionLaunched(session);

		println("Launched");
	}

	@Override
	public void sessionStarted(final ITestRunSession session) {
		super.sessionStarted(session);

		this.time = System.currentTimeMillis();
		this.failures = 0;
		final String port = ArduinoUnitPreferences.getPort();
		this.enabled = port != null;
		if (this.enabled) {
			try {
				this.comm.open(port);
			} catch (final Exception e) {
				e.printStackTrace();
				this.enabled = false;
				this.comm.close();
			}
		}

		if (this.enabled) {
			try {
				this.comm.write(-1); // started

				final int count = getTestCount(session.getChildren());
				this.comm.write(count); // number of tests
			} catch (final Exception e) {
				e.printStackTrace();
				this.enabled = false;
				this.comm.close();
			}
		}
		println("Started " + (System.currentTimeMillis() - this.time));
	}

	@Override
	public void sessionFinished(final ITestRunSession session) {
		super.sessionFinished(session);
		if (this.enabled) {
			try {
				this.comm.write(-2); // finished
				this.comm.close();
				this.enabled = false;
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		println("Finished " + (System.currentTimeMillis() - this.time));
		printResults(session);
	}

	@Override
	public void testCaseStarted(final ITestCaseElement testCaseElement) {
		super.testCaseStarted(testCaseElement);
		println("Started " + testCaseElement.getTestClassName() + "." + testCaseElement.getTestMethodName() + " "
				+ (System.currentTimeMillis() - this.time));
	}

	@Override
	public void testCaseFinished(final ITestCaseElement testCaseElement) {
		super.testCaseFinished(testCaseElement);

		if (this.enabled) {
			try {
				if (testCaseElement.getFailureTrace() != null) {
					this.comm.write(-3); // failed
				} else {
					this.comm.write(-4); // succeeded
				}
			} catch (final Exception e) {
				e.printStackTrace();
				this.enabled = false;
				this.comm.close();
			}
		}

		println("Ended " + testCaseElement.getTestClassName() + "." + testCaseElement.getTestMethodName() + " "
				+ (System.currentTimeMillis() - this.time));
		if (testCaseElement.getFailureTrace() != null) {
			failures++;
		}
	}
}
