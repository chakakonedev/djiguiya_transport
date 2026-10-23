package com.djiguiya.transport.modules.staff.repository;

import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Persistence for {@link StaffEntity} records. */
public interface StaffRepository extends JpaRepository<StaffEntity, String> {

	List<StaffEntity> findByStatusOrderByFullNameAsc(String status);
}
