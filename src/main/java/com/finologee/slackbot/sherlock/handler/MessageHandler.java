package com.finologee.slackbot.sherlock.handler;

import com.finologee.slackbot.sherlock.service.SlackMessageService;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.methods.MethodsClient;
import com.slack.api.model.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 * When a message is received from slack
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

	private final SlackMessageService slackMessageService;

	@Async
	public void handleMessageEvent(MethodsClient client, EventsApiPayload<MessageEvent> payload, String channelId) {
		log.info("#handleMessageEvent user={}", payload.getEvent().getUser());
		var text = payload.getEvent().getText().toLowerCase();
		if (Stream.of("my", "status").allMatch(text::contains)) {
			wantsUserJiraStatus(client, payload, channelId, payload.getEvent().getUser());
		}
		if (Stream.of("team", "status").allMatch(text::contains)) {
			wantsTeamJiraStatus(channelId);
		}
	}

	public void wantsTeamJiraStatus(String channelId) {
		log.info("#wantsTeamJiraStatus");
		slackMessageService.sendJiraStatusForTeam(channelId);
	}

	public void wantsUserJiraStatus(MethodsClient client, EventsApiPayload<MessageEvent> payload, String channelId, String userId) {
		log.info("#wantsUserJiraStatus userId={} channelId={}", userId, channelId);
		slackMessageService.sendJiraStatusForUser(userId, channelId);
	}

}
