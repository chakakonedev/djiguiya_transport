package com.djiguiya.transport.config;

import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Order(100)
@EnableConfigurationProperties(CoreStaffSeedProperties.class)
@RequiredArgsConstructor
@Slf4j
public class CoreStaffSeeder implements ApplicationRunner {

	private final CoreStaffSeedProperties properties;
	private final UserRepository userRepository;
	private final StaffRepository staffRepository;
	private final ProfitShareRuleRepository profitShareRuleRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!properties.getSeed().isCoreStaffEnabled()) {
			return;
		}

		String defaultPassword = properties.getSecurity().getDefaultPassword();
		if (defaultPassword == null || defaultPassword.isBlank()) {
			throw new IllegalStateException("djiguiya.security.default-password must be set when core staff seed is enabled");
		}

		for (CoreStaffDefinitions.Person person : CoreStaffDefinitions.PEOPLE) {
			upsertUser(person, defaultPassword);
			upsertStaff(person);
		}
		for (CoreStaffDefinitions.ShareRule rule : CoreStaffDefinitions.SHARE_RULES) {
			upsertShareRule(rule);
		}

		log.info("Core staff seed applied ({} people, {} share rules)",
				CoreStaffDefinitions.PEOPLE.size(),
				CoreStaffDefinitions.SHARE_RULES.size());
	}

	private void upsertUser(CoreStaffDefinitions.Person person, String defaultPassword) {
		UserEntity user = userRepository.findById(person.userId()).orElseGet(UserEntity::new);
		boolean created = user.getId() == null;
		if (created) {
			user.setId(person.userId());
			user.setCreatedAt(Instant.now());
			user.setPasswordChanged(false);
		}

		user.setEmail(person.email());
		user.setFullName(person.fullName());
		user.setRoleCode(person.roleCode());
		user.setActive(true);
		user.setUpdatedAt(Instant.now());

		if (!user.isPasswordChanged()) {
			if (created || user.getPasswordHash() == null
					|| !passwordEncoder.matches(defaultPassword, user.getPasswordHash())) {
				user.setPasswordHash(passwordEncoder.encode(defaultPassword));
			}
		}

		userRepository.save(user);
	}

	private void upsertStaff(CoreStaffDefinitions.Person person) {
		StaffEntity staff = staffRepository.findById(person.staffId()).orElseGet(StaffEntity::new);
		if (staff.getId() == null) {
			staff.setId(person.staffId());
		}
		staff.setFullName(person.fullName());
		staff.setStaffType(person.roleCode());
		staff.setStatus("active");
		staff.setMonthlySalary(person.monthlySalary());
		staff.setPayDay(person.payDay());
		staff.setUserId(person.userId());
		staff.setNotes(person.notes());
		staffRepository.save(staff);
	}

	private void upsertShareRule(CoreStaffDefinitions.ShareRule definition) {
		ProfitShareRuleEntity rule = profitShareRuleRepository.findById(definition.id()).orElseGet(ProfitShareRuleEntity::new);
		if (rule.getId() == null) {
			rule.setId(definition.id());
		}
		rule.setBeneficiaryStaffId(definition.beneficiaryStaffId());
		rule.setRuleKind(definition.ruleKind());
		rule.setRatePercent(definition.ratePercent());
		rule.setSalaryMonthsEquivalent(definition.salaryMonthsEquivalent());
		rule.setEnabled(true);
		rule.setEffectiveFrom(CoreStaffDefinitions.EFFECTIVE_FROM);
		rule.setEffectiveTo(null);
		rule.setNotes(definition.notes());
		profitShareRuleRepository.save(rule);
	}
}
