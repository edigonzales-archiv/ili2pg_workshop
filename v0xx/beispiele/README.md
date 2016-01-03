Beispiel 01
===========

* SO-Modell wegen Checker.
* Linienattribute! Siehe 254900_export_sta.txt. Einziger Unterschied zum Original-Logfile. Im exportierten ITF ist ein @ bei `Gemeindegrenze_Geometrie`. Im Original ist eine 0.
* Umsteigen auf Bundesmodell -> Login für private E-Mail-Adresse und anschliessend testen, ob 1000 Fehlermeldungen kommen.


Beispiel 02
===========

* wieder Bundesmodell (einfacher wegen Styles)
* t_id readonly falls serial in DB
* --log für Vergleich Statistik
* passt: import 5nf und 0 projbofl / export 6nf und 1 projbofl

* Claude fragen: Import ITF. Erfassen eines neuen Objektes. Händische Vergabe der t_id. Beim ITF-Export interpretier ili2pg meine händische t_id als höchste t_id und vergibt automatisch korrekte Werte bei den Geometrie-Spaghettis?
