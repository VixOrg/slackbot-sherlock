package com.finologee.slackbot.sherlock.handler;

import com.finologee.slackbot.sherlock.service.SlackMessageService;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.methods.MethodsClient;
import com.slack.api.model.event.AppMentionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppMentionHandler {

	private final SlackMessageService slackMessageService;

	@Async
	public void handleAppMentionEvent(MethodsClient client, EventsApiPayload<AppMentionEvent> payload, String channelId) {
		log.info("#handleAppMentionEvent user={}", payload.getEvent().getUser());
		var text = payload.getEvent().getText();
		if (Stream.of("team", "status").allMatch(text::contains)) {
			wantsTeamJiraStatus(client, payload, channelId);
		}
	}


	public void wantsTeamJiraStatus(MethodsClient client, EventsApiPayload<AppMentionEvent> payload, String channelId) {
		log.info("#wantsTeamJiraStatus");
		slackMessageService.sendJiraStatusForTeam(channelId);
	}

}
