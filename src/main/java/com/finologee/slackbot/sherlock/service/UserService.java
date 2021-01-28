package com.finologee.slackbot.sherlock.service;

import com.finologee.slackbot.sherlock.config.props.UserProperties;
import com.finologee.slackbot.sherlock.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserProperties userProperties;

	public User findBySlackId(String slackId) {
		return userProperties.getUsers()
				.stream()
				.filter(u -> u.getSlackId().equals(slackId))
				.findFirst()
				.orElseThrow();
	}
}
