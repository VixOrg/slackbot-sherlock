package com.finologee.slackbot.sherlock.web;

import javax.ws.rs.QueryParam;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.finologee.slackbot.sherlock.service.JiraSprintStatusService;
import com.finologee.slackbot.sherlock.service.JiraStatusService;

/**
 * Not used, was used to test locally
 */
@Profile("local")
@Slf4j
@RestController
@RequiredArgsConstructor
public class JiraController {
    private final JiraStatusService jiraStatusService;
    private final JiraSprintStatusService jiraSprintStatusService;

    @GetMapping("/sprint-status-test")
    private String sprintStatusTest(@QueryParam("project") String project) {
        return jiraSprintStatusService.buildProjectSprintStatusReport(null, project);
    }
}
