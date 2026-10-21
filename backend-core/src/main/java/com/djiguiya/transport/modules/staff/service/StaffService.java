package com.djiguiya.transport.modules.staff.service;

import com.djiguiya.transport.modules.staff.dto.StaffRequest;
import com.djiguiya.transport.modules.staff.dto.StaffResponse;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import com.djiguiya.transport.shared.exception.BusinessException;
import com.djiguiya.transport.shared.util.EntityIds;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

	private final StaffRepository staffRepository;

	@Transactional(readOnly = true)
	public List<StaffResponse> listAll() {
		return staffRepository.findAll().stream().map(StaffResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public StaffResponse getById(String id) {
		return StaffResponse.from(findEntity(id));
	}

	@Transactional
	public StaffResponse create(StaffRequest request) {
		StaffEntity entity = new StaffEntity();
		entity.setId(EntityIds.newId());
		apply(entity, request);
		return StaffResponse.from(staffRepository.save(entity));
	}

	@Transactional
	public StaffResponse update(String id, StaffRequest request) {
		StaffEntity entity = findEntity(id);
		apply(entity, request);
		return StaffResponse.from(staffRepository.save(entity));
	}

	public StaffEntity findEntity(String id) {
		return staffRepository.findById(id)
				.orElseThrow(() -> new BusinessException("staff.not_found", HttpStatus.NOT_FOUND));
	}

	private void apply(StaffEntity entity, StaffRequest request) {
		entity.setFullName(request.fullName().trim());
		entity.setStaffType(request.staffType().trim());
		entity.setPhone(request.phone());
		entity.setStatus(request.status() != null ? request.status() : "active");
		entity.setMonthlySalary(request.monthlySalary());
		entity.setPayDay(request.payDay());
		entity.setUserId(request.userId());
		entity.setNotes(request.notes());
	}
}
