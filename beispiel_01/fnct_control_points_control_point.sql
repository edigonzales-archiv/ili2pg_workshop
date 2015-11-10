--DROP FUNCTION control_points_control_point(text, integer);
CREATE OR REPLACE FUNCTION control_points_control_point(dbschema text, bfsnr integer)
  RETURNS boolean AS
$$
BEGIN
  EXECUTE
'
WITH lfp1 AS (
SELECT a.t_id, 0 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen,
  CASE
    WHEN a.punktzeichen IS NULL THEN 7
    ELSE a.punktzeichen
  END as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am,
a.gem_bfs as bfsnr
FROM av_avdpool_ng.fixpunktekategorie1_lfp1 as a,
     av_avdpool_ng.fixpunktekategorie1_lfp1nachfuehrung as b
WHERE a.gem_bfs = '||bfsnr||'
AND b.gem_bfs = '||bfsnr||'
AND a.entstehung = b.t_id
),
lfp2 AS (
SELECT a.t_id, 2 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen,
  CASE
    WHEN a.punktzeichen IS NULL THEN 7
    ELSE a.punktzeichen
  END as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am,
a.gem_bfs as bfsnr
FROM av_avdpool_ng.fixpunktekategorie2_lfp2 as a,
     av_avdpool_ng.fixpunktekategorie2_lfp2nachfuehrung as b
WHERE a.gem_bfs = '||bfsnr||'
AND b.gem_bfs = '||bfsnr||'
AND a.entstehung = b.t_id
),
lfp3 AS (
SELECT a.t_id, 4 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen,
  CASE
    WHEN a.punktzeichen IS NULL THEN 7
    ELSE a.punktzeichen
  END as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am,
a.gem_bfs as bfsnr
FROM av_avdpool_ng.fixpunktekategorie3_lfp3 as a,
     av_avdpool_ng.fixpunktekategorie3_lfp3nachfuehrung as b
WHERE a.gem_bfs = '||bfsnr||'
AND b.gem_bfs = '||bfsnr||'
AND a.entstehung = b.t_id
),
hfp1 AS (
SELECT a.t_id, 1 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen, 8 as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am,
a.gem_bfs as bfsnr
FROM av_avdpool_ng.fixpunktekategorie1_hfp1 as a,
     av_avdpool_ng.fixpunktekategorie1_hfp1nachfuehrung as b
WHERE a.gem_bfs = '||bfsnr||'
AND b.gem_bfs = '||bfsnr||'
AND a.entstehung = b.t_id
),
hfp2 AS (
SELECT a.t_id, 3 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen, 8 as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am,
a.gem_bfs as bfsnr
FROM av_avdpool_ng.fixpunktekategorie2_hfp2 as a,
     av_avdpool_ng.fixpunktekategorie2_hfp2nachfuehrung as b
WHERE a.gem_bfs = '||bfsnr||'
AND b.gem_bfs = '||bfsnr||'
AND a.entstehung = b.t_id
),
hfp3 AS (
SELECT a.t_id, 5 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen, 8 as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am,
a.gem_bfs as bfsnr
FROM av_avdpool_ng.fixpunktekategorie3_hfp3 as a,
     av_avdpool_ng.fixpunktekategorie3_hfp3nachfuehrung as b
WHERE a.gem_bfs = '||bfsnr||'
AND b.gem_bfs = '||bfsnr||'
AND a.entstehung = b.t_id
)

INSERT INTO '||dbschema||'.control_points_control_point (t_id, category, identnd, anumber, plan_accuracy, geom_alt,
       alt_accuracy, mark, state_of, fosnr, geometry)
SELECT nextval('''||dbschema||'.t_id'') AS t_id, kategorie as category, nbident as identnd, nummer as anumber,
       lagegen as plan_accuracy, hoehegeom as geom_alt, hoehegen as alt_accuracy, punktzeichen as mark,
       stand_am::timestamp without time zone as state_of,
       bfsnr as fosnr, geometrie as geometry
FROM
(
 SELECT * FROM lfp1
 UNION ALL
 SELECT * FROM lfp2
 UNION ALL
 SELECT * FROM lfp3
 UNION ALL
 SELECT * FROM hfp1
 UNION ALL
 SELECT * FROM hfp2
 UNION ALL
 SELECT * FROM hfp3
) as foo;
';

  RETURN TRUE;
END;
$$
LANGUAGE 'plpgsql';
