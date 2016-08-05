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
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Destination;
import javax.jms.TextMessage;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

/**
 * Message-Driven Bean that simply replies to messages.
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Queue")
		})
public class Receiver implements MessageListener {

	@Resource(name="jms/connectionFactory", type=javax.jms.ConnectionFactory.class)
	private ConnectionFactory connectionFactory;

	public void onMessage(Message message) {
		Connection connection = null;
		try {
			System.out.println("Message Received!!!");

			Destination replyDestination = message.getJMSReplyTo();

			connection = connectionFactory.createConnection();
			Session session = connection.createSession(true, 0);
			MessageProducer producer = session.createProducer(replyDestination);
			TextMessage replyMessage = session.createTextMessage();

			replyMessage.setText("Here is your response sender");

			System.out.println("Sending reply");
			producer.send(replyMessage);

		} catch (JMSException jmse) {
			System.out.println("Exception occurred: " + jmse.getMessage());
		} finally {
			if (connection != null) {
				System.out.println("Closing connection");
				try {
					connection.close();
				} catch (JMSException exc) {
				}
			}
		}
	}

}
