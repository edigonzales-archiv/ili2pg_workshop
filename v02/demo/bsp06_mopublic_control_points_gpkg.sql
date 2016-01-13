SELECT t_id, t_id as t_ili_tid, kategorie as category, nbident as identnd, nummer as anumber,
       lagegen as plan_accuracy, hoehegeom as geom_alt, hoehegen as alt_accuracy, punktzeich as mark,
       stand_am as state_of,
       2549 as fosnr, the_geom as geometry
FROM
(
 SELECT a.t_id, 4 as kategorie, a.nbident, nummer, a.the_geom, lagegen, hoehegeom, hoehegen,
   CASE
     WHEN a.punktzeich IS NULL THEN 7
     ELSE a.punktzeich
   END as punktzeich,
   CASE
     WHEN b.gueltigere IS NULL THEN b.datum1
     ELSE b.gueltigere
   END AS stand_am
 FROM fixpunktekategorie3_lfp3 as a,
      fixpunktekategorie3_lfp3nachfuehrung as b
 WHERE a.entstehung = b.t_id

UNION

 SELECT a.t_id, 2 as kategorie, a.nbident, nummer, a.the_geom, lagegen, hoehegeom, hoehegen,
   CASE
     WHEN a.punktzeich IS NULL THEN 7
     ELSE a.punktzeich
   END as punktzeich,
   CASE
     WHEN b.gueltigere IS NULL THEN b.datum1
     ELSE b.gueltigere
   END AS stand_am
 FROM fixpunktekategorie2_lfp2 as a,
      fixpunktekategorie2_lfp2nachfuehrung as b
 WHERE a.entstehung = b.t_id
) as lfp
