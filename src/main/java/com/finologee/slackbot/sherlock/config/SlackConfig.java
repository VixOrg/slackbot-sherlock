package com.finologee.slackbot.sherlock.config;

import com.finologee.slackbot.sherlock.config.props.SlackProperties;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

	@Autowired
	private SlackProperties slackProperties;

	@Bean
	public MethodsClient methodsClient() {
		Slack slack = Slack.getInstance();
		return slack.methods(slackProperties.getToken());
	}
}
