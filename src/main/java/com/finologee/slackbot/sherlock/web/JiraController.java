package com.finologee.slackbot.sherlock.web;

import com.finologee.slackbot.sherlock.service.JiraStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

/**
 * Not used, was used to test locally
 */
@Profile("local")
@Slf4j
@RestController
@RequiredArgsConstructor
public class JiraController {
	private final JiraStatusService jiraStatusService;
}
