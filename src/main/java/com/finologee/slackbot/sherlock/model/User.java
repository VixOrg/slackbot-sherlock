package com.finologee.slackbot.sherlock.model;

import lombok.Data;

@Data
public class User {
	private String slackId;
	private String jiraId;
	private Boolean wantsJiraStatusInChannel = false;
}
