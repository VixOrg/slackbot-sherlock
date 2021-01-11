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
		try {
			var statusText = jiraStatusService.buildStatusForAllUsers();
			log.info("Posting team status to channel {}", channelId);
			slackClient.chatPostMessage(r -> r
					.channel(channelId)
					.text(statusText));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
