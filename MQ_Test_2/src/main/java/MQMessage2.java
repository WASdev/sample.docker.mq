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
package receiveAndSend;
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
 * Message-Driven Bean implementation class for: MQMessage2
 * This application sits and waits for a message then sends a reply using jms over MQ Messenger
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Queue")
		})
public class MQMessage2 implements MessageListener {

	@Resource(name="jms/sample", type=javax.jms.ConnectionFactory.class)
	private ConnectionFactory myCF;

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	public void onMessage(Message message) {
		try {

			// System out that MDB had been driven
			System.out.println("Message Received!!!");

			// Get the destination to send a response to, hope its not null...
			Destination replyDestination = message.getJMSReplyTo();

			// Create the required JMS connections etc
			Connection replyConnection = myCF.createConnection();
			Session replySession = replyConnection.createSession(true, 0);
			MessageProducer replyProducer = replySession.createProducer(replyDestination);

			// Create a response message and populate it
			TextMessage replyMessage = replySession.createTextMessage();

			replyMessage.setText("Here is your response liberty1");

			// Send the reply
			replyProducer.send(replyMessage);
			System.out.println("Sending reply...");
			// Close off JMS items
			System.out.println("Closing connection....");
			replySession.close();
			replyConnection.close();

		} catch (JMSException jmse) {
			// oh dear.....
			System.out.println("Something went wrong.... " + jmse);
		}
		System.out.println("Waiting for new message!");
	}

}
