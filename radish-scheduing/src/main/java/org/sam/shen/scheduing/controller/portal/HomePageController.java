package org.sam.shen.scheduing.controller.portal;

import javax.annotation.Resource;

import org.sam.shen.scheduing.entity.Company;
import org.sam.shen.scheduing.mapper.CompanyMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Controller
@RequestMapping(value="portal")
public class HomePageController {
	
	@Resource
	private CompanyMapper companyMapper;

	@RequestMapping(value = {"", "/", "/index", "/home"}, method = RequestMethod.GET)
	public ModelAndView home(ModelAndView model) {
		model.setViewName("home");
		return model;
	}
	
	@RequestMapping(value = "dashboard", method = RequestMethod.GET)
	public ModelAndView dashboard(ModelAndView model) {
		Page<Company> page = new Page<>();
		page = companyMapper.selectCompanyPager(page);
		model.addObject("page", page);
		model.setViewName("frame/dashboard");
		return model;
	}
	
}
