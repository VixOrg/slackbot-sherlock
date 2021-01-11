package com.finologee.slackbot.sherlock.web;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackAppServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet("/slack/events")
public class SlackAppServletImpl extends SlackAppServlet {

	public SlackAppServletImpl(App app) {
		super(app);
	}

}
