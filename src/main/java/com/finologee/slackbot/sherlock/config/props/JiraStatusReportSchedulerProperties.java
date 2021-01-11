package com.finologee.slackbot.sherlock.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
@ConfigurationProperties(prefix = "app.scheduler.jira-status")
public class JiraStatusReportSchedulerProperties {
	@NotEmpty
	private String cron;
	@NotEmpty
	private String channelId;
}
