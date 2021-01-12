package com.finologee.slackbot.sherlock.scheduler;

import com.finologee.slackbot.sherlock.config.props.JiraStatusReportSchedulerProperties;
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

	private final TaskScheduler taskScheduler;
	private final JiraStatusReportSchedulerProperties jiraStatusReportSchedulerProperties;
	private final SlackMessageService slackMessageService;

	@PostConstruct
	public void reportStatus() {
		var zone = ZoneId.of("Europe/Luxembourg");
		var cron = new CronTrigger(jiraStatusReportSchedulerProperties.getCron(), zone);
		log.info("Will sendJiraStatusForTeam with cron={}, zone={}", jiraStatusReportSchedulerProperties.getCron(),zone.getId());
		taskScheduler.schedule(() -> slackMessageService.sendJiraStatusForTeam(jiraStatusReportSchedulerProperties
				.getChannelId()), cron);
	}

}
