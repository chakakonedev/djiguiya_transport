package com.djiguiya.transport.modules.staff.repository;

import com.djiguiya.transport.modules.staff.entities.TeamLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamLinkRepository extends JpaRepository<TeamLinkEntity, String> {

	List<TeamLinkEntity> findByManagerId(String managerId);

	List<TeamLinkEntity> findByDriverId(String driverId);
}
