package com.finologee.slackbot.sherlock.config;

import com.finologee.slackbot.sherlock.config.props.SlackProperties;
import com.finologee.slackbot.sherlock.handler.MessageHandler;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.model.event.AppMentionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Slf4j
@Configuration
public class SlackAppServerConfig {

	@Autowired
	private MessageHandler messageHandler;

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
			messageHandler.handleMessage(ctx.client(), payload.getEvent()
					.getText(), payload.getEvent().getUser(), ctx.getChannelId(), true);
			return ctx.ack();
		});
		app.message(Pattern.compile("^.*"), (payload, ctx) -> {
			log.info("Received message");
			messageHandler.handleMessage(ctx.client(), payload.getEvent()
					.getText(), payload.getEvent().getUser(), ctx.getChannelId(), false);
			return ctx.ack();
		});
		return app;
	}


}

