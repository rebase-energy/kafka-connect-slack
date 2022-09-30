package net.mongey.kafka.connect

import org.apache.kafka.common.config.AbstractConfig
import org.apache.kafka.common.config.ConfigDef

import org.slf4j.LoggerFactory;

class SlackSinkConnectorConfig : AbstractConfig  {
    constructor(props: Map<String, String>) : super(CONFIG, props)

    companion object {
        private val log = LoggerFactory.getLogger(SlackSinkConnectorConfig::class.java);

        @JvmStatic
        val CONFIG = baseConfigDef()
    
        protected fun baseConfigDef(): ConfigDef {
            val configDef = ConfigDef()
            addConnectorConfigs(configDef)
            return configDef
        }

        fun addConnectorConfigs(configDef: ConfigDef) {
            val group = "Connector"
            var order = 0            
            configDef.define(
                    "slack.token",
                    ConfigDef.Type.STRING,
                    ConfigDef.Importance.HIGH,
                    "It's the most importatnt thing",
                    group,
                    ++order,
                    ConfigDef.Width.SHORT,
                    "Slack Token"
            ).define(
                    "slack.username",
                    ConfigDef.Type.STRING,
                    ConfigDef.Importance.HIGH,
                    "Send to user",
                    group,
                    ++order,
                    ConfigDef.Width.SHORT,
                    "Slack User"
            ).define(
                    "slack.channel",
                    ConfigDef.Type.STRING,
                    ConfigDef.Importance.HIGH,
                    "Send to channel",
                    group,
                    ++order,
                    ConfigDef.Width.SHORT,
                    "Slack User"
            ).define(
                    "message.template",
                    ConfigDef.Type.STRING,
                    ConfigDef.Importance.HIGH,
                    "Template to use",
                    group,
                    ++order,
                    ConfigDef.Width.SHORT,
                    "Template"
            )
        }
    }
}