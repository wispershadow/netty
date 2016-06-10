package org.wispersd.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wispersd.commonplatform.infra.http.entity.PostForm;
import org.wispersd.commonplatform.infra.http.entity.PostRequest;

@Controller
@RequestMapping(value="/testpost")
public class TestPostController {
	
	@RequestMapping(value="submitJsonReq", method=RequestMethod.POST)
	public @ResponseBody String doPostJson(@RequestBody PostRequest postReq) {
		System.out.println(postReq);
		return postReq.getRequestId();
	}
	
	@RequestMapping(value="submitFormReq", method=RequestMethod.POST)
	public @ResponseBody String doPostForm(@ModelAttribute PostForm postForm) {
		System.out.println(postForm);
		return postForm.getRequestId();
	}
}
