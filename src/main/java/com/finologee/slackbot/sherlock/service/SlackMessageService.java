package com.finologee.slackbot.sherlock.service;


import com.finologee.slackbot.sherlock.config.props.UserProperties;
import com.finologee.slackbot.sherlock.model.User;
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
	private final UserProperties userProperties;

	public void sendJiraStatusForTeam(String channelId) {
		log.info("#sendJiraStatusForTeam");
		for (User user : userProperties.getUsers()) {
			var statusText = jiraStatusService.buildStatusForUser(user);
			log.info("Posting user status to channel {}", channelId);
			sendMessage(statusText, channelId);
			threadSleep(1000);
		}
		log.info("End of posting team status in {}", channelId);
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


	private void threadSleep(long time) {
		try {
			log.info("Sleeping {} millis", time);
			Thread.sleep(time);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
