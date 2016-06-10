package org.wispersd.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wispersd.commonplatform.infra.http.entity.TestObj;

@Controller
@RequestMapping(value="/testget")
public class TestGetController {

	@RequestMapping(value="findById", method=RequestMethod.GET)
	public @ResponseBody TestObj getTestObject(@RequestParam(value="param1", required=false) String param1, @RequestParam(value="param2", required=false) String param2) {
		System.out.println("param1: " + param1 + "  param2: " + param2 + " " + Thread.currentThread().getName());
		TestObj result = new TestObj();
		result.setA(param1);
		result.setB((int)(Math.random() * 1000));
		return result;
	}
}
