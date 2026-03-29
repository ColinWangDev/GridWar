INSERT INTO seasons (started_at, ends_at)
VALUES (NOW(), NOW() + INTERVAL '7 days');

INSERT INTO cells (x, y)
SELECT gx, gy
FROM generate_series(0, 29) AS gx
CROSS JOIN generate_series(0, 29) AS gy;
