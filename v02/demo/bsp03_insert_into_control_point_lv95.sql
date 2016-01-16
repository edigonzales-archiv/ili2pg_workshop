WITH lfp3 AS (
SELECT a.t_id, 4 as kategorie, a.nbident, nummer, geometrie, lagegen, hoehegeom, hoehegen,
  CASE
    WHEN a.punktzeichen IS NULL THEN 7
    ELSE a.punktzeichen
  END as punktzeichen,
  CASE
    WHEN b.gueltigereintrag IS NULL THEN b.datum1
    ELSE b.gueltigereintrag
  END AS stand_am
FROM beispiel03.fixpunktekategorie3_lfp3 as a,
     beispiel03.fixpunktekategorie3_lfp3nachfuehrung as b
WHERE a.entstehung = b.t_id
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
  END AS stand_am
FROM beispiel03.fixpunktekategorie2_lfp2 as a,
     beispiel03.fixpunktekategorie2_lfp2nachfuehrung as b
WHERE a.entstehung = b.t_id
)

--INSERT INTO beispiel03_export.control_points_control_point (t_id, category, identnd, anumber, plan_accuracy, geom_alt,
--       alt_accuracy, mark, state_of, fosnr, geometry)
SELECT t_id, kategorie as category, nbident as identnd, nummer as anumber,
       lagegen as plan_accuracy, hoehegeom as geom_alt, hoehegen as alt_accuracy, punktzeichen as mark,
       stand_am::timestamp without time zone as state_of,
       2549 as fosnr, ST_Fineltra(geometrie, 'av_chenyx06.chenyx06_triangles', 'the_geom_lv03', 'the_geom_lv95') as geometry
FROM
(
 SELECT * FROM lfp3
 UNION ALL
 SELECT * FROM lfp2
) as foo;
