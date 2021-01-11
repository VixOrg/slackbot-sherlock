package com.finologee.slackbot.sherlock.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.finologee.slackbot.sherlock.config.props.UserProperties;
import com.finologee.slackbot.sherlock.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

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
	private final UserProperties userProperties;

	public String buildStatusForAllUsers() {
		StringBuilder allStatuses = new StringBuilder();
		for (User user : userProperties.getUsers()) {
			var userStatus = buildStatusForUser(user);
			allStatuses.append(userStatus).append("\n");
		}
		return allStatuses.toString();
	}

	public String buildStatusForUserBySlackId(String slackId) {
		User user = userProperties.getUsers()
				.stream()
				.filter(u -> u.getSlackId().equals(slackId))
				.findFirst()
				.orElseThrow();
		return buildStatusForUser(user);
	}

	public String buildStatusForUser(User user) {
		return String.format("Here is the status of <@%s> \n", user.getSlackId()) +
				"What has been done recently :sunglasses: \n" +
				buildDoneStatusForUser(user) +
				"What is in progress :female-construction-worker: \n" +
				buildInProgressStatusForUser(user) +
				"What is next :rocket: \n" +
				buildNextItemStatusForUser(user);
	}


	/**
	 * Represents the issues for the user that will be handled soon
	 */
	private String buildNextItemStatusForUser(User user) {
		StringBuilder statusText = new StringBuilder();
		var vars = Map.of("user", user.getJiraId());
		var jqlTemplate = "assignee = ${user} AND status in (\"Groomed\", \"Dev On Hold\", \"Backlog\") ORDER BY priority DESC ";
		var jql = StringSubstitutor.replace(jqlTemplate, vars, "${", "}");
		return buildIssueReportForQuery(jql, 5);
	}

	/**
	 * Represents the issues for the user that are in progress
	 */
	private String buildInProgressStatusForUser(User user) {
		StringBuilder statusText = new StringBuilder();
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


	private String buildDoneStatusForUser(User user) {
		StringBuilder statusText = new StringBuilder();
		var vars = Map.of("user", user.getJiraId());
		var jqlTemplate = "assignee was ${user} " +
				" AND status in (\"Ready for testing\", \"Testing In Progress\", \"Testing Done\", \"Testing Blocked\", Closed) " +
				" AND status changed from (\"In Progress\", \"Selected For Development\", Backlog, Groomed, \"Dev In Progress\", \"Dev On Hold\", \"Code Review\") to (\"Ready for testing\", \"Testing In Progress\", \"Testing Done\", \"Testing Blocked\", Closed) after -72h " +
				" ORDER BY updated DESC ";
		var jql = StringSubstitutor.replace(jqlTemplate, vars, "${", "}");
		return buildIssueReportForQuery(jql);
	}

	private String buildIssueLine(Issue issue) {
		return " • <https://finologee.atlassian.net/browse/" + issue.getKey() + "|" + issue
				.getKey() + ">" +
				" (" + issue.getStatus().getName() + ") : " +
				issue.getSummary();
	}

}
