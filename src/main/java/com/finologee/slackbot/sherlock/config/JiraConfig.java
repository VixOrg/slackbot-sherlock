package com.finologee.slackbot.sherlock.config;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.finologee.slackbot.sherlock.config.props.JiraProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class JiraConfig {

	@Autowired
	private JiraProperties jiraProperties;

	@Bean
	public JiraRestClient jiraRestClient() {
		var client = new AsynchronousJiraRestClientFactory()
				.create(URI.create(jiraProperties.getUrl()),
						new BasicHttpAuthenticationHandler(
								jiraProperties.getUsername(),
								jiraProperties.getToken()));
		return client;
	}

}
