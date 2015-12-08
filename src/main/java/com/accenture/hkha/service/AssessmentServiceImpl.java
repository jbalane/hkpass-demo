package com.accenture.hkha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.accenture.hkha.dao.AssessmentDao;
import com.accenture.hkha.model.Assessment;

@Service("assessmentService")
public class AssessmentServiceImpl implements AssessmentService {

	AssessmentDao assessmentDao;
	
	@Autowired
	public void setWorkDao(AssessmentDao assessmentDao) {
		this.assessmentDao = assessmentDao;
	}

	@Override
	public List<Assessment> findAll() {
		return assessmentDao.findAll();
	}

	@Override
	public Assessment findById(Integer id) {
		return assessmentDao.findById(id);
	}

	@Override
	public void saveOrUpdate(Assessment assessment) {
		if(findById(assessment.getId()) == null){
			assessmentDao.save(assessment);
		}else{
			assessmentDao.update(assessment);
		}

	}

	@Override
	public List<Assessment> findByUser(String user) {
		return assessmentDao.findByUser(user);
	}
	
	@Override
	public List<Assessment> findByStatus(String status) {
		return assessmentDao.findByStatus(status);
	}
	
	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}




}