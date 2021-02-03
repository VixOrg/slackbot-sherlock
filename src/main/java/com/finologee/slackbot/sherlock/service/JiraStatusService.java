package com.finologee.slackbot.sherlock.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.finologee.slackbot.sherlock.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class is able to inspect statuses in jira
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraStatusService {

	private final JiraRestClient jiraRestClient;
	private final UserService userService;

	public String buildStatusForUserBySlackId(String slackId) {
		return buildStatusForUser(userService.findBySlackId(slackId));
	}

	public String buildWeeklyStatusForUserBySlackId(String slackId) {
		return buildWeeklyStatusForUser(userService.findBySlackId(slackId));
	}

	public String buildStatusForUser(User user) {
		LocalDate localDate = LocalDate.now();
		return String.format("Here is the *daily* status of <@%s> for *%s*\n", user.getSlackId(), localDate
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) +
				"What has been done last day :sunglasses: \n" +
				buildDoneStatusForUser(user, computeHoursPastForDone(24)) +
				"What is in progress :female-construction-worker: \n" +
				buildInProgressStatusForUser(user) +
				"What is next :rocket: \n" +
				buildNextItemStatusForUser(user) +
				"\n--";
	}

	public String buildWeeklyStatusForUser(User user) {
		LocalDate toDate = LocalDate.now();
		var fromDate = toDate.minusWeeks(1);
		return String.format("Here is the *weekly* status of <@%s> from *%s* to *%s* \n", user.getSlackId(), fromDate
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) +
				"What has been done *last 7 days* :sunglasses: \n" +
				buildDoneStatusForUser(user, 168)  // last 7 days
				+ "\n--";
	}

	/**
	 * Represents the issues for the user that will be handled soon
	 */
	public String buildLastReleasesReport() {
		var jqlTemplate = "project in (Digicash, \"Digital Onboarding\", Mpulse, \"PSD2 Hub\", \"Micro Services\", \"KYC Manager\", ENPAY) AND type in (Epic, Release) AND cf[10075] is not EMPTY ORDER BY cf[10075] DESC ";
		return buildIssueReportForReleases(jqlTemplate, 10);
	}

	/**
	 * Represents the issues for the user that will be handled soon
	 */
	private String buildNextItemStatusForUser(User user) {
		var vars = Map.of("user", user.getJiraId());
		var jqlTemplate = "assignee = ${user} AND status in (\"Groomed\", \"Dev On Hold\", \"Backlog\") ORDER BY priority DESC ";
		var jql = StringSubstitutor.replace(jqlTemplate, vars, "${", "}");
		return buildIssueReportForQuery(jql, 5);
	}

	/**
	 * Represents the issues for the user that are in progress
	 */
	private String buildInProgressStatusForUser(User user) {
		var vars = Map.of("user", user.getJiraId());
		var jqlTemplate = "assignee = ${user} AND status in (\"In Progress\", \"Dev In Progress\", \"Code Review\") ORDER BY updated DESC ";
		var jql = StringSubstitutor.replace(jqlTemplate, vars, "${", "}");
		return buildIssueReportForQuery(jql);
	}

	private String buildIssueReportForQuery(String jql) {
		return buildIssueReportForQuery(jql, Integer.MAX_VALUE);
	}

	private String buildIssueReportForQuery(String jql, int limit) {
		var response = jiraRestClient.getSearchClient().searchJql(jql);
		var statusText = new StringBuilder();
		try {
			var issues = StreamSupport
					.stream(response.get().getIssues().spliterator(), false)
					.collect(Collectors.toList());
			issues.stream()
					.limit(limit)
					.forEach(issue -> statusText.append(buildIssueLine(issue)).append("\n"));
			if (issues.size() > limit) {
				statusText.append(" • ...").append(issues.size() - limit).append(" more items\n");
			}

			if (issues.size() == 0) {
				statusText.append(" • (empty)\n");
			}
			return statusText.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Builds the slack report for done items
	 *
	 * @param user
	 * @param lastHours - the last hours to consider for done status
	 * @return the slack report for done items
	 */
	private String buildDoneStatusForUser(User user, Integer lastHours) {
		var vars = Map.of("user", user.getJiraId(), "hoursInPast", lastHours.toString());
		var jqlTemplate = "assignee was ${user} " +
				" AND status in (\"Ready for testing\", \"Testing In Progress\", \"Testing Done\", \"Testing Blocked\", Closed) " +
				" AND status changed from (\"In Progress\", \"Selected For Development\", Backlog, Groomed, \"Dev In Progress\", \"Dev On Hold\", \"Code Review\") to (\"Ready for testing\", \"Testing In Progress\", \"Testing Done\", \"Testing Blocked\", Closed) after -${hoursInPast}h " +
				" ORDER BY updated DESC ";
		var jql = StringSubstitutor.replace(jqlTemplate, vars, "${", "}");
		return buildIssueReportForQuery(jql);
	}

	/**
	 * Depending on the day of the week, the hours in past change (weekend)
	 */
	private Integer computeHoursPastForDone(Integer lastHours) {
		Integer hours = lastHours;
		int numberOfDays = lastHours / 24;
		var previousDate = LocalDate.now();
		while (numberOfDays > 0) {
			previousDate = previousDate.minusDays(1);
			if (DayOfWeek.of(previousDate.get(ChronoField.DAY_OF_WEEK)).equals(DayOfWeek.SATURDAY)
					|| DayOfWeek.of(previousDate.get(ChronoField.DAY_OF_WEEK))
					.equals(DayOfWeek.SUNDAY)) {
				hours += 24;
			}
			else {
				numberOfDays -= 1;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("#computeHoursPastForDone lastHours={}, hours={}", lastHours, hours);
		}
		return hours;
	}

	private String buildIssueLine(Issue issue) {
		return " • <https://finologee.atlassian.net/browse/" + issue.getKey() + "|" + issue
				.getKey() + ">" +
				" (" + issue.getStatus().getName() + ") : " +
				issue.getSummary();
	}

	private String buildReleaseIssueLine(Issue issue) {
		return " • [" + formatDate((String) issue.getFieldByName("Release start date")
				.getValue()) + "] <https://finologee.atlassian.net/browse/" + issue.getKey() + "|" + issue
				.getKey() + ">" +
				" (" + issue.getStatus().getName() + ") : " +
				issue.getSummary();
	}

	private String formatDate(String releaseEndDate) {
		if (StringUtils.isNotEmpty(releaseEndDate)) {
			var localDateTime = LocalDateTime.parse(releaseEndDate.substring(0, 16), DateTimeFormatter
					.ofPattern("yyyy-MM-dd'T'HH:mm"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
			return localDateTime.format(formatter);
		}
		else {
			return "empty";
		}
	}

	private String buildIssueReportForReleases(String jql, int limit) {
		var response = jiraRestClient.getSearchClient().searchJql(jql);
		var statusText = new StringBuilder();
		try {
			var issues = StreamSupport
					.stream(response.get().getIssues().spliterator(), false)
					.collect(Collectors.toList());
			issues.stream()
					.limit(limit)
					.forEach(issue -> statusText.append(buildReleaseIssueLine(issue)).append("\n"));
			if (issues.size() == 0) {
				statusText.append(" • (empty)\n");
			}
			return statusText.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
