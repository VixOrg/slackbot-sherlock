package com.finologee.slackbot.sherlock.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is able to inspect statuses in jira
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSprintStatusService {

	private final JiraRestClient jiraRestClient;

	public String buildProjectSprintStatusReport(String userId, String project) {
		if (StringUtils.isBlank(project)) {
			return "No project - no status! Elementary, my dear  <@"+ userId +">!\n"
					+ "A good start would be asking me for \"help\" ;)";
		}
		
		try {
			
			var projects = StreamSupport.stream(jiraRestClient.getProjectClient().getAllProjects().get().spliterator(), false)
					.map(BasicProject::getKey).map(String::toUpperCase).collect(Collectors.toList());
			if(!projects.contains(project.toUpperCase())) {
				return "The project \"" + project + "\" does not exist in Jira!\n"
						+ "A good start would be asking me for \"help\" ;)";
			}

			var jql = "project = \"" + project + "\" and Sprint in openSprints() and issuetype not in subTaskIssueTypes() order by created DESC";
			var response = jiraRestClient.getSearchClient().searchJql(jql, 500, null, null);

			var issues = StreamSupport.stream(response.get().getIssues().spliterator(), false).collect(Collectors.toList());

			if (issues.size() == 0) {
				return "No issues found in open sprints\n";
			}

			var statusText = new StringBuilder();

			statusText.append(buildTicketStatusSummary(project, issues));
			statusText.append(buildEstimateSummary(issues, "Σ Original Estimate"));
			statusText.append(buildEstimateSummary(issues, "Σ Remaining Estimate"));
			statusText.append(buildEstimateSummary(issues, "Σ Time Spent"));

			return statusText.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Integer getIssueOriginalEstimate(Issue issue, String fieldName) {
		Object value = issue.getFieldByName(fieldName).getValue();
		if (value == null) {
			return 0;
		} else {
			return Integer.valueOf(value.toString());
		}
	}

	private String buildTicketStatusSummary(String project, List<Issue> issues) {
		var totalIssueCount = issues.stream().count();

		var toDoStatuses = List.of("BACKLOG", "GROOMED", "SELECTED FOR DEVELOPMENT");
		var toDoIssueCount = issues.stream().filter(issue -> toDoStatuses.contains(issue.getStatus().getName().toUpperCase())).count();

		var devInProgressStatuses = List.of("DEV ON HOLD", "DEV IN PROGRESS", "CODE REVIEW", "BA IN PROGRESS", "IN PROGRESS");
		var devInProgressIssueCount = issues.stream().filter(issue -> devInProgressStatuses.contains(issue.getStatus().getName().toUpperCase())).count();

		var inTestingStatuses = List.of("READY FOR TESTING", "TESTING IN PROGRESS", "TESTING BLOCKED");
		var inTestingIssueCount = issues.stream().filter(issue -> inTestingStatuses.contains(issue.getStatus().getName().toUpperCase())).count();

		var doneStatuses = List.of("TESTING DONE", "CLOSED");
		var doneIssueCount = issues.stream().filter(issue -> doneStatuses.contains(issue.getStatus().getName().toUpperCase())).count();

		if ((toDoIssueCount + devInProgressIssueCount + inTestingIssueCount + doneIssueCount) != totalIssueCount) {
			log.warn("#buildProjectSprintStatusReport -> total issue count is not equal to sum of status breakdown!", new IllegalStateException());
		}

		// 12 issues in open sprint(s) in mp:
		// 		4 Todo
		// 		6 Dev in progress
		// 		1 In testing
		// 		0 Done

		var statusText = new StringBuilder();
		statusText.append(totalIssueCount).append(" issues in open sprint(s) in ").append(project)
				.append(":\n • Todo: ").append(toDoIssueCount)
				// .append("(").append(toDoStatuses.stream().collect(Collectors.joining(","))).append(")")
				.append("\n • Dev in progress: ").append(devInProgressIssueCount)
				// .append("(").append(devInProgressStatuses.stream().collect(Collectors.joining(","))).append(")")
				.append("\n • In testing: ").append(inTestingIssueCount)
				// .append("(").append(inTestingStatuses.stream().collect(Collectors.joining(","))).append(")")
				.append("\n • Done: ").append(doneIssueCount)
				// .append("(").append(doneStatuses.stream().collect(Collectors.joining(","))).append(")")
				.append("\n\n");

		return statusText.toString();
	}

	private String buildEstimateSummary(List<Issue> issues, String fieldName) {
		int sum = issues.stream().map(issue -> getIssueOriginalEstimate(issue, fieldName)).collect(Collectors.summingInt(Integer::intValue));
		return new StringBuilder(fieldName)
				.append(": ")
				.append(sum / 60 / 60)
				.append("h\n").toString();
	}
	
	
	/**
	 * Parses the project for sprint status. Knows how to parse below:
	 * - enpay sprint status
	 * - what is enpay sprint status
	 * - give me ENPAY sprint status
	 * 
	 * @param text
	 * @return
	 */
	public static String parseProject(String text) {
		String textBeforeSprintStatus = StringUtils.trimToEmpty(StringUtils.substringBeforeLast(text, "sprint status"));
		String project = textBeforeSprintStatus;
		if(textBeforeSprintStatus.contains(" ")) {
			project = StringUtils.substringAfterLast(textBeforeSprintStatus, " ");
		}
		return project;
	}
}
