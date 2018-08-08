package org.sam.shen.scheduing.mapper;

import org.sam.shen.scheduing.entity.Company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface CompanyMapper extends BaseMapper<Company> {
	
	Page<Company> selectCompanyPager(Page<Company> page);
}
