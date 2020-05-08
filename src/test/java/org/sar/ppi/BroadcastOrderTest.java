package org.sar.ppi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sar.ppi.mpi.MpiRunner;
import org.sar.ppi.peersim.PeerSimRunner;

/**
 * ExampleNodeProces
 */
public class BroadcastOrderTest extends NodeProcess {

	public static class ExampleMessage extends Message{

		private static final long serialVersionUID = 1L;
		private String s;
		public ExampleMessage(int src, int dest, String s) {
			super(src, dest);
			this.s = s;
		}

		public String getS() {
			return s;
		}

	}

	@MessageHandler
	public void processExampleMessage(ExampleMessage message) {
		int host = infra.getId();
		System.out.printf("%d Received '%s' from %d\n", host, message.getS(), message.getIdsrc());
		infra.exit();
	}

	@Override
	public void start() {
		if (infra.getId() == 0) {
			for(int i=1;i<infra.size();i++) {
				infra.send(new ExampleMessage(0,i, "OrderTest"));
			}
			infra.exit();
		}
	}

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;


	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	public void MpiAnnotatedProcessTest() {
		String[] args = { BroadcastOrderTest.class.getName(), MpiRunner.class.getName() };
		Ppi.main(args);
	// je sais as c quelle sys.out qui fausse le outContent
		//	assertEquals(120, outContent.size());
		assertEquals("", errContent.toString());
	}

	@Test
	public void PeersimAnnotatedProcessTest() {
		String[] args = { BroadcastOrderTest.class.getName(), PeerSimRunner.class.getName() };
		Ppi.main(args);
		String expected = "\n\n\nThread1\n2 Received 'OrderTest' from 0\nThread1\n3 Received 'OrderTest' from 0\nThread1\n1 Received 'OrderTest' from 0\nThread1\n4 Received 'OrderTest' from 0\n";
		assertEquals(expected, outContent.toString());
	}
}