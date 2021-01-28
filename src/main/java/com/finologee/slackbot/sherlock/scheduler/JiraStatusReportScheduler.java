package com.finologee.slackbot.sherlock.scheduler;

import com.finologee.slackbot.sherlock.config.props.JiraStatusReportSchedulerProperties;
import com.finologee.slackbot.sherlock.config.props.JiraWeeklyStatusReportSchedulerProperties;
import com.finologee.slackbot.sherlock.service.SlackMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class JiraStatusReportScheduler {

	private final static ZoneId ZONE_LUXEMBOURG = ZoneId.of("Europe/Luxembourg");
	private final TaskScheduler taskScheduler;
	private final JiraStatusReportSchedulerProperties jiraStatusReportSchedulerProperties;
	private final JiraWeeklyStatusReportSchedulerProperties jiraWeeklyStatusReportSchedulerProperties;
	private final SlackMessageService slackMessageService;

	@PostConstruct
	public void reportStatus() {
		scheduleDailyStatus();
		scheduleWeeklyStatus();
	}

	private void scheduleDailyStatus() {
		var cron = new CronTrigger(jiraStatusReportSchedulerProperties.getCron(), ZONE_LUXEMBOURG);
		log.info("Will sendJiraStatusForTeam with cron={}, zone={}", jiraStatusReportSchedulerProperties
				.getCron(), ZONE_LUXEMBOURG.getId());
		taskScheduler.schedule(() -> slackMessageService.sendJiraStatusForUsersThatAgree(jiraStatusReportSchedulerProperties
				.getChannelId()), cron);
	}

	private void scheduleWeeklyStatus() {
		var cron = new CronTrigger(jiraWeeklyStatusReportSchedulerProperties.getCron(), ZONE_LUXEMBOURG);
		log.info("Will sendWeeklyJiraStatusForTeam with cron={}, zone={}", jiraWeeklyStatusReportSchedulerProperties
				.getCron(), ZONE_LUXEMBOURG.getId());
		taskScheduler.schedule(() -> slackMessageService.sendWeeklyJiraStatusForUsersThatAgree(jiraWeeklyStatusReportSchedulerProperties
				.getChannelId()), cron);
	}

}
