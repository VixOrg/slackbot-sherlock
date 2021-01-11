package com.finologee.slackbot.sherlock.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
@ConfigurationProperties(prefix = "app.jira")
public class JiraProperties {
	@NotEmpty
	private String username;
	@NotEmpty
	private String token;
	@NotEmpty
	private String url;
}
