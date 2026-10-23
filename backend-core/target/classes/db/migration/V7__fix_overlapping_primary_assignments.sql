-- Close older open primary assignments when several rows are open for the same vehicle.
UPDATE primary_assignments pa
	INNER JOIN (
		SELECT vehicle_id, MAX(start_date) AS latest_start
		FROM primary_assignments
		WHERE end_date IS NULL
		GROUP BY vehicle_id
		HAVING COUNT(*) > 1
	) dup ON dup.vehicle_id = pa.vehicle_id
SET pa.end_date = CASE
		WHEN DATE_SUB(dup.latest_start, INTERVAL 1 DAY) < pa.start_date THEN pa.start_date
		ELSE DATE_SUB(dup.latest_start, INTERVAL 1 DAY)
	END
WHERE pa.end_date IS NULL
	AND pa.start_date < dup.latest_start;
