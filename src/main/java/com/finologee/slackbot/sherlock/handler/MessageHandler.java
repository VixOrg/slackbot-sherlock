package com.finologee.slackbot.sherlock.handler;

import com.finologee.slackbot.sherlock.service.JiraSprintStatusService;
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
		if (Stream.of("my status").allMatch(text.toLowerCase()::contains)) {
			wantsUserJiraStatus(channelId, userId);
		}
		if (Stream.of("my weekly status").allMatch(text.toLowerCase()::contains)) {
			wantsUserWeeklyJiraStatus(channelId, userId);
		}
		if (Stream.of("team status").allMatch(text.toLowerCase()::contains)) {
			wantsTeamJiraStatus(channelId);
		}
		if (Stream.of("team weekly status").allMatch(text.toLowerCase()::contains)) {
			wantsTeamWeeklyJiraStatus(channelId);
		}
		if (Stream.of("hello", "hi").anyMatch(text.toLowerCase()::contains)) {
			displayGreetings(channelId, userId);
		}
		if (Stream.of("last release").allMatch(text.toLowerCase()::contains)) {
			wantsLastReleases(channelId);
		}
		if (Stream.of("sprint status").allMatch(text.toLowerCase()::contains)) {
			wantsSprintStatus(channelId, userId, JiraSprintStatusService.parseProject(text.toLowerCase()));
		}
		if (Stream.of("help").allMatch(text.toLowerCase()::contains)) {
			displayHelp(channelId, userId);
		}
	}

	public void wantsTeamJiraStatus(String channelId) {
		log.info("#wantsTeamJiraStatus");
		slackMessageService.sendJiraStatusForTeam(channelId);
	}

	public void wantsTeamWeeklyJiraStatus(String channelId) {
		log.info("#wantsTeamWeeklyJiraStatus");
		slackMessageService.sendWeeklyJiraStatusForTeam(channelId);
	}

	public void wantsUserJiraStatus(String channelId, String userId) {
		log.info("#wantsUserJiraStatus userId={} channelId={}", userId, channelId);
		slackMessageService.sendJiraStatusForUser(userId, channelId);
	}

	public void wantsUserWeeklyJiraStatus(String channelId, String userId) {
		log.info("#wantsUserWeeklyJiraStatus userId={} channelId={}", userId, channelId);
		slackMessageService.sendWeeklyJiraStatusForUser(userId, channelId);
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
		message.append(String.format("Here is how I can help you sir <@%s>:\n", userId));
		message.append(" • hi, hello - greetings\n");
		message.append(" • h e l p (no spaces) - display this message\n");
		message.append(" • my status - display my status using Jira\n");
		message.append(" • my weekly status - display my weekly status\n");
		message.append(" • team status - display the team status \n");
		message.append(" • team weekly status - display team weekly status\n");
		message.append(" • last release - display last releases from Jira\n");
		message.append(" • <project> sprint status  - display the summary of the open sprint of <project> in Jira.\n\t\tExample: enpay sprint status\n");
		message.append("That's it!");
		slackMessageService.sendMessage(message.toString(), channelId);
	}
}
