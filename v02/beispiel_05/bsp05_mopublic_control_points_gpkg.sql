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
FROM beispiel05.fixpunktekategorie3_lfp3 as a,
     beispiel05.fixpunktekategorie3_lfp3nachfuehrung as b
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
FROM beispiel05.fixpunktekategorie2_lfp2 as a,
     beispiel05.fixpunktekategorie2_lfp2nachfuehrung as b
WHERE a.entstehung = b.t_id
)

--INSERT INTO control_points_control_point (T_Id, T_Ili_id, Category, IdentND, anumber, Plan_accuracy, Geom_alt,
--       Alt_accuracy, Mark, State_of, FOSNr, Geometry)
SELECT t_id, t_id as t_ili_tid, kategorie as category, nbident as identnd, nummer as anumber,
       lagegen as plan_accuracy, hoehegeom as geom_alt, hoehegen as alt_accuracy, punktzeichen as mark,
       stand_am::timestamp without time zone as state_of,
       2549 as fosnr, geometrie as geometry
FROM
(
 SELECT * FROM lfp3
 UNION ALL
 SELECT * FROM lfp2
) as foo;
