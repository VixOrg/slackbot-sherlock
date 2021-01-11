package com.finologee.slackbot.sherlock.service;


import com.slack.api.methods.MethodsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackMessageService {

	private final JiraStatusService jiraStatusService;
	private final MethodsClient slackClient;

	public void sendJiraStatusForTeam(String channelId) {
		log.info("#sendJiraStatusForTeam");
		var statusText = jiraStatusService.buildStatusForAllUsers();
		log.info("Posting team status to channel {}", channelId);
		sendMessage(statusText, channelId);
	}

	public void sendJiraStatusForUser(String userId, String channelId) {
		log.info("#sendJiraStatusForUser");
		var statusText = jiraStatusService.buildStatusForUserBySlackId(userId);
		log.info("Posting user status to channel {}", channelId);
		sendMessage(statusText, channelId);
	}


	public void sendMessage(String message, String channelId) {
		try {
			slackClient.chatPostMessage(r -> r
					.channel(channelId)
					.text(message));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
