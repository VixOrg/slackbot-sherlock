package com.finologee.slackbot.sherlock.handler;

import java.util.stream.Stream;

import com.slack.api.methods.MethodsClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.finologee.slackbot.sherlock.service.JiraSprintStatusService;
import com.finologee.slackbot.sherlock.service.SlackMessageService;

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
		if (Stream.of("my status").allMatch(text.toLowerCase()::contains)) {
			wantsUserJiraStatus(channelId, userId);
		}
		if (Stream.of("team status").allMatch(text.toLowerCase()::contains)) {
			wantsTeamJiraStatus(channelId);
		}
		if (Stream.of("hello", "hi").anyMatch(text.toLowerCase()::contains)) {
			displayGreetings(channelId, userId);
		}
		if (Stream.of("last release").allMatch(text.toLowerCase()::contains)) {
			wantsLastReleases(channelId);
		}
		if (Stream.of("sprint status").allMatch(text.toLowerCase()::contains)) {
			wantsSprintStatus(channelId, userId, JiraSprintStatusService.parseProject(text));
		}
		if (Stream.of("help").allMatch(text.toLowerCase()::contains)) {
			displayHelp(channelId, userId);
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

	public void wantsSprintStatus(String channelId, String userId, String project) {
		log.info("#wantsSprintStatus channelId={}", channelId);
		slackMessageService.sendProjectSprintStatusReport(channelId, userId, project);
	}

	public void displayGreetings(String channelId, String userId) {
		log.info("#wantsGreetings userId={} channelId={}", userId, channelId);
		slackMessageService.sendMessage(String.format("Elementary, my dear <@%s>!", userId), channelId);
	}
	
	public void displayHelp(String channelId, String userId) {
		log.info("#wantsHelp userId={} channelId={}", userId, channelId);
		var message = new StringBuilder();
		message.append(String.format("Here is how I can help you sir <@%s>:", userId));
		message.append(" • hi, hello - greetings");
		message.append(" • h e l p (no spaces) - display this message");
		message.append(" • my status - display my status using Jira");
		message.append(" • team status - display the team status (for users opted in to be in the wall of fame)");
		message.append(" • last release - display last releases from Jira");
		message.append(" • <project> sprint status  - display the summary of the open sprint of <project> in Jira. Example: enpay sprint status");
		slackMessageService.sendMessage(message.toString(), channelId);
	}
}
