package net.mongey.kafka.connect;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.apache.kafka.connect.json.JsonConverter;
import java.nio.charset.StandardCharsets;
import java.util.*;


class SlackSinkTask : SinkTask() {
    var config : Map<String, String> = HashMap(1);
    var slack : Slack? = null;
    var slackMethodsClient : MethodsClient? = null;

    override fun version(): String {
//        return Version.getVersion()
        return "0.0.1";
    }

    override fun start(props: Map<String, String>) {
        start(props, null)
        this.config = props;
        val slack = Slack.getInstance();
        this.slackMethodsClient = slack.methods(this.config.get("slack.token"));
        this.slack = slack;
    }

    // public for testing
    fun start(props: Map<String, String>, client: Any?) {
        log.info("Starting SlackSinkTask.");
    }

    override fun open(partitions: Collection<TopicPartition>) {
        log.debug("Opening the task for topic partitions: {}", partitions);
    }

    @Throws(ConnectException::class)
    override fun put(records: Collection<SinkRecord>) {
        log.trace("Putting {} to Slack.", records);
        val configUser = this.config.get("slack.username");
        val configChannel = this.config.get("slack.channel");

        log.info("ConfigUser: " + configUser + ", configChannel: " + configChannel);
        for (record in records) {
            log.trace("Kafka Message: {}",record.toString());
            val recordData = recordToMap(record);
            val t = config.get("message.template");
            val defaultTemplate = "No Template found.";
            val template: String = t ?: defaultTemplate;

            var str = format(template, recordData);

            log.info("RecordData: " + recordData.toString() + ", str: " + str)

            if (recordData.isEmpty()) {
                log.error("Unable to convert record data into templatable message, skipping {}, {}", recordData, record);
                continue;
            }

            if (configChannel != null) {
                val request = ChatPostMessageRequest.builder()
                        .channel(configChannel) 
                        .text(str)
                        .build();
                val response = this.slackMethodsClient?.chatPostMessage(request);
            } else {
                log.error("channel was null $configChannel");
            }

            if (configUser != null && configUser != "") {
                log.error("Sending to users not implemented yet.");
            }
        }
    }

    override fun flush(offsets: Map<TopicPartition, OffsetAndMetadata>?) {
        log.trace("Flushing data to Slack with the following offsets: {}", offsets);
    }

    override fun close(partitions: Collection<TopicPartition>) {
        log.debug("Closing the task for topic partitions: {}", partitions);
    }

    @Throws(ConnectException::class)
    override fun stop() {
        log.info("Stopping SlackSinkTask.");
    }

    companion object {
        private val log = LoggerFactory.getLogger(SlackSinkTask::class.java);
    }
}

fun recordToMap(record: SinkRecord): Map<String, String> {
    val schema = record.valueSchema()
    val value = record.value()
    val JSON_CONVERTER = JsonConverter()
    JSON_CONVERTER.configure(Collections.singletonMap("schemas.enable", "false"), false)

    val normalizedMap = HashMap<String,String>()
    val rawJsonPayload = JSON_CONVERTER.fromConnectData(record.topic(), schema, value)

    if (rawJsonPayload == null) {
      return normalizedMap
    }

    val jsonStr =  String(rawJsonPayload, StandardCharsets.UTF_8)
    val flattenJson = JsonFlattener.flattenAsMap(jsonStr)

    for (p in flattenJson) {
        normalizedMap.set(p.key,p.value.toString())
    }
    return normalizedMap
}

fun format(template: String, data: Map<String, String>): String {
    return StrSubstitutor(data).replace(template)
}
