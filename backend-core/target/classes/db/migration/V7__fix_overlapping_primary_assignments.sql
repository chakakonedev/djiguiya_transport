-- Close older open primary assignments when several rows are open for the same vehicle.
-- Correlated UPDATE + TIMESTAMPADD: works on MySQL and H2 MODE=MySQL (no UPDATE..JOIN, no DATE_SUB).
UPDATE primary_assignments
SET end_date = CASE
		WHEN TIMESTAMPADD(DAY, -1, (
			SELECT latest_start
			FROM (
				SELECT vehicle_id AS vid, MAX(start_date) AS latest_start
				FROM primary_assignments
				WHERE end_date IS NULL
				GROUP BY vehicle_id
				HAVING COUNT(*) > 1
			) dup
			WHERE dup.vid = primary_assignments.vehicle_id
		)) < start_date THEN start_date
		ELSE TIMESTAMPADD(DAY, -1, (
			SELECT latest_start
			FROM (
				SELECT vehicle_id AS vid, MAX(start_date) AS latest_start
				FROM primary_assignments
				WHERE end_date IS NULL
				GROUP BY vehicle_id
				HAVING COUNT(*) > 1
			) dup
			WHERE dup.vid = primary_assignments.vehicle_id
		))
	END
WHERE end_date IS NULL
	AND vehicle_id IN (
		SELECT vid
		FROM (
			SELECT vehicle_id AS vid
			FROM primary_assignments
			WHERE end_date IS NULL
			GROUP BY vehicle_id
			HAVING COUNT(*) > 1
		) dups
	)
	AND start_date < (
		SELECT latest_start
		FROM (
			SELECT vehicle_id AS vid, MAX(start_date) AS latest_start
			FROM primary_assignments
			WHERE end_date IS NULL
			GROUP BY vehicle_id
		) latest
		WHERE latest.vid = primary_assignments.vehicle_id
	);
