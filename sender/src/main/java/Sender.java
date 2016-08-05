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
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * A simple application that sends a request message and then waits for, and
 * receives, the reply.
 */
@WebServlet("/")
public class Sender extends HttpServlet {

	@Resource(name="jms/connectionFactory", type=javax.jms.ConnectionFactory.class)
	private ConnectionFactory connectionFactory;

	@Resource(name="jms/queue", type=javax.jms.Queue.class)
	private Destination destination;

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println("<h1>Liberty - MQ - Liberty</h1>\n");
		output(out, "Results:");

		output(out, "Creating connection to QueueManager");
		Connection connection = null;
		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(destination);
			Destination tempDestination = session.createTemporaryQueue();

			String text = "Give me something back receiver";
			output(out, "Sending message request of '" + text + "'");

			TextMessage message = session.createTextMessage(text);
			message.setJMSReplyTo(tempDestination);
			producer.send(message);
			output(out, "Sent Message ID=" + message.getJMSMessageID());

			output(out, "Waiting for response");
			connection.start();
			MessageConsumer consumer = session.createConsumer(tempDestination);
			Message receivedMessage = consumer.receive(15 * 1000);
			if (receivedMessage != null) {
				output(out, "Received Message ID=" + receivedMessage.getJMSMessageID() + " for '" + ((TextMessage) receivedMessage).getText() + "'");
			} else {
				output(out, "No response message received in 15 seconds");
			}

		} catch (Exception exc) {
			output(out, "Exception occurred: " + exc.getLocalizedMessage());
		} finally {
			output(out, "Closing Connection");
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException jmsex) {
				}
			}
		}
	}

	private void output(PrintWriter out, String message) {
		System.out.println("> " + message);
		out.println(message + "<br>");
		out.flush();
	}

}
