/** (C) Copyright IBM Corporation 2015.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import java.io.IOException;
import java.io.PrintWriter;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * A simple application that sends a request message and then wait for, and
 * receive, the reply. Note: It is assumed that some other application will process the request
 * message and send the reply message.
 * 
 * Notes:
 * 
 * API type: JMS API (v1.1, unified domain)
 * 
 * Messaging domain: Point-to-point
 * 
 * Provider type: WebSphere MQ + WebSphere Liberty
 * 
 * Connection mode: Client connection
 * 
 * JNDI in use: Yes
 * 
 */
@WebServlet("/MQMessage")
public class MQMessage extends HttpServlet {

	private static final long serialVersionUID = 1L;
	// Variables
	Connection connection = null;
	ConnectionFactory connectionFactory = null;
	Session session = null;
	Destination destination = null;
	Destination tempDestination = null;
	MessageProducer producer = null;
	MessageConsumer consumer = null;
	PrintWriter out;
	// System exit status value (assume unset value to be 1)

	/**
	 * Main method 
	 * Invoked when a get request is made
	 * 
	 * @param args
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		out = response.getWriter();
		out.println("<H1>Liberty - MQ - Liberty + Liberty Profile</H1>\n");

		try {
			out.println("Results:");
			// Instantiate the initial context
			Context context = new InitialContext();

			// Lookup the connection factory
			System.out.println("> Locating Connection factory.");
			out.println("Locating Connection factory<br>");
			connectionFactory = (ConnectionFactory) context.lookup("jms/sample");

			// Output Result
			System.out.println("> Connection factory located in JNDI.");
			out.println("Connection factory located in JNDI<br>");

			// Lookup the destination
			System.out.println("> Locating The Destination.");
			out.println("Locating The Destination<br>");
			destination = (Destination) context.lookup("jms/queue1");

			// Output Result
			out.println("Destination located in JNDI<br>");
			System.out.println("> Destination located in JNDI.");

			// Create JMS objects
			System.out.println("> Creating connection to QueueManager.");
			out.println("Creating connection to QueueManager<br>");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Output Result
			out.println("Connection created<br>");
			System.out.println("> Connection created.");

			// Register an exception listener on the connection
			connection.setExceptionListener(new ExceptionListener() {

				public void onException(JMSException arg0) {
					out.println("Exception sent to exception listener<br>");
					System.out.println("Exception sent to exception listener");

				}
			});

			// Create JMS producer & use temporary Queue for the responce message
			producer = session.createProducer(destination);
			tempDestination = session.createTemporaryQueue();


			// Create JMS consumer
			consumer = session.createConsumer(tempDestination);

			String text = "Give me something back liberty2";
			out.println("Sending message request of '" + text + "'" + "<br>");
			System.out.println("\n> Sending message request of '" + text + "'");
			TextMessage message = session
					.createTextMessage(text);

			// Set the JMSReplyTo
			message.setJMSReplyTo(tempDestination);

			// Start the connection
			connection.start();

			// Send the request
			producer.send(message);
			System.out.println("> Sent Message ID=" + message.getJMSMessageID());
			out.println("Sent Message ID=" + message.getJMSMessageID() + "<br>");

			// Now, receive the reply
			out.println("Waiting for message... <br>");
			Message receivedMessage = consumer.receive(15000 * 1000); // in ms or 15 seconds
			if (receivedMessage != null) {
				System.out.println("\n> Received Message ID=" + receivedMessage.getJMSMessageID() + " for '" + ((TextMessage) receivedMessage).getText() + "'");
			}
			else {
				System.out.println("\n! No response message received in 15 seconds.");
			}

		}
		catch (NamingException ne) {
			out.println("The initial context could not be instantiated, or the lookup failed <br>");
			out.println(ne + "<br>");
			System.out.println("The initial context could not be instantiated, or the lookup failed.");
			System.out.println(ne);
			ne.printStackTrace();
		}
		catch (JMSException jmsex) {
		}
		finally {
			out.println("Closing connection to QueueManager<br>");
			System.out.println("\n> Closing connection to QueueManager.");
			if (producer != null) {
				try {
					producer.close();
				}
				catch (JMSException jmsex) {
					out.println("Producer could not be closed<br>");
					System.out.println("Producer could not be closed.");
				}
			}
			if (consumer != null) {
				try {
					consumer.close();
				}
				catch (JMSException jmsex) {
					out.println("Consumer could not be closed<br>");
					System.out.println("Consumer could not be closed.");
				}
			}

			if (session != null) {
				try {
					session.close();
				}
				catch (JMSException jmsex) {
					out.println("Session could not be closed<br>");
					System.out.println("Session could not be closed.");
				}
			}

			if (connection != null) {
				try {
					connection.close();
				}
				catch (JMSException jmsex) {
					out.println("Connection could not be closed<br>");
					System.out.println("Connection could not be closed.");
					System.out.println(jmsex.getErrorCode());
				}
			}
			out.println("Closed Connection<br>");
			System.out.println("> Closed Connection.");
		}
	} // End main()
}
