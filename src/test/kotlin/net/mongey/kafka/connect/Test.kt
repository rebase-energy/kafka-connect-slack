package net.mongey.kafka.connect

import org.junit.Test
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

class TestDetector {
    @Test
    fun test() {
        val template = "Dear \${name}, your \${ability} is over \${numbers}!"
        val data = hashMapOf("name" to "Vegeta", "ability" to "power", "numbers" to "9000")

        val expected = "Dear Vegeta, your power is over 9000!"
        val actual = format(template, data)
        assertThat(actual, `is`(expected))
    }

	@Test
	fun shouldCreateConnector() {
		var connector = SlackSinkConnector();
		val config = HashMap<String, String>(1);
		config.put("connector.class", "net.mongey.kafka.connect.SlackSinkConnector");
		config.put("topics", "datahub-status");
		config.put("slack.token", "test");
		config.put("slack.channel", "test-channel");
		config.put("slack.username", "");
		config.put("message.template", "Message received: \${message}");
		connector.start(config);

		var taskConfigs: List<Map<String, String>> = connector.taskConfigs(2); 
		assertThat(taskConfigs.size, `is`(2));
	}

}
