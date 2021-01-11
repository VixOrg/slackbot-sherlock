package com.finologee.slackbot.sherlock.config;

import com.finologee.slackbot.sherlock.config.props.SlackProperties;
import com.finologee.slackbot.sherlock.handler.AppMentionHandler;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.model.event.AppMentionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SlackAppServerConfig {

	@Autowired
	private AppMentionHandler appMentionEventHandler;

	@Autowired
	private SlackProperties slackProperties;

	@Bean
	public App initSlackApp() {
		App app = new App(AppConfig.builder()
				.singleTeamBotToken(slackProperties.getToken())
				.signingSecret(slackProperties.getSigningSecret())
				.build());
		app.event(AppMentionEvent.class, (payload, ctx) -> {
			log.info("Received event app_mention");
			appMentionEventHandler.handleAppMentionEvent(ctx.client(), payload, ctx.getChannelId());
			return ctx.ack();
		});
		return app;
	}


}

