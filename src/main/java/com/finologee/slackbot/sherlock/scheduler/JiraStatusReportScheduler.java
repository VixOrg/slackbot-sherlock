package com.finologee.slackbot.sherlock.scheduler;

import com.finologee.slackbot.sherlock.config.props.JiraStatusReportSchedulerProperties;
import com.finologee.slackbot.sherlock.service.SlackMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class JiraStatusReportScheduler {

	private final TaskScheduler taskScheduler;
	private final JiraStatusReportSchedulerProperties jiraStatusReportSchedulerProperties;
	private final SlackMessageService slackMessageService;

	@PostConstruct
	public void reportStatus() {
		log.info("Will sendJiraStatusForTeam with cron={}", jiraStatusReportSchedulerProperties.getCron());
		taskScheduler.schedule(
				() -> slackMessageService.sendJiraStatusForTeam(jiraStatusReportSchedulerProperties.getChannelId()),
				new CronTrigger(jiraStatusReportSchedulerProperties.getCron()));
	}

}
