package com.finologee.slackbot.sherlock.handler;

import com.finologee.slackbot.sherlock.service.SlackMessageService;
import com.slack.api.methods.MethodsClient;
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
	public void handleMessage(MethodsClient client, String text, String userId, String channelId, boolean mention) {
		log.info("#handleMessageEvent user={}", userId);
		var textAsLowerCase = text.toLowerCase();
		if (Stream.of("my status").allMatch(textAsLowerCase::contains)) {
			wantsUserJiraStatus(channelId, userId);
		}
		if (Stream.of("team status").allMatch(text::contains)) {
			wantsTeamJiraStatus(channelId);
		}
		if (Stream.of("hello", "hi").anyMatch(text::contains)) {
			greetings(channelId, userId);
		}
		if (Stream.of("last releases").allMatch(text::contains)) {
			wantsLastReleases(channelId);
		}
	}

	public void wantsTeamJiraStatus(String channelId) {
		log.info("#wantsTeamJiraStatus");
		slackMessageService.sendJiraStatusForTeam(channelId);
	}

	public void wantsUserJiraStatus(String channelId, String userId) {
		log.info("#wantsUserJiraStatus userId={} channelId={}", userId, channelId);
		slackMessageService.sendJiraStatusForUser(userId, channelId);
	}

	public void wantsLastReleases(String channelId) {
		log.info("#wantsLastReleases channelId={}", channelId);
		slackMessageService.sendLastReleasesReport(channelId);
	}

	public void greetings(String channelId, String userId) {
		log.info("#wantsGreetings userId={} channelId={}", userId, channelId);
		slackMessageService.sendMessage(String.format("Elementary, my dear <@%s>!", userId), channelId);
	}

}
