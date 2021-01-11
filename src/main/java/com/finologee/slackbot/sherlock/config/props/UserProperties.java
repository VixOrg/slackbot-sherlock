package com.finologee.slackbot.sherlock.config.props;

import com.finologee.slackbot.sherlock.model.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@ConfigurationProperties(prefix = "app")
public class UserProperties {
	private List<User> users;
}
