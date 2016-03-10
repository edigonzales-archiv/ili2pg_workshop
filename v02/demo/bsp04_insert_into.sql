/*

-- Verwaltungseinheiten

INSERT INTO beispiel04.buildings_administration (t_id, department_name)
VALUES (1001, 'Amt für Geoinformation');

INSERT INTO beispiel04.buildings_administration (t_id, department_name)
VALUES (1002, 'Amt für Umwelt');


-- Wohngebäude

INSERT INTO beispiel04.buildings_apartmentbuilding (t_id, apartments, storeys, roof, geometry) 
VALUES (1, 10, 5, 'shed', ST_PolygonFromText('POLYGON((607946 228269, 607945 228280, 607956 228282, 607958 228272, 607946 228269))', 21781));

INSERT INTO beispiel04.buildings_apartmentbuilding (t_id, apartments, storeys, roof, geometry) 
VALUES (2, 6, 4, 'shed', ST_PolygonFromText('POLYGON((607938 228311, 607941 228299, 607950 228302, 607949 228312, 607938 228311))', 21781));


INSERT INTO beispiel04.buildings_address (t_id, t_seq, house_number, street_name, regbl_egid, buildings_prtmntblding_addresses) 
VALUES (101, 0, '24', 'Schänzlistrasse', NULL, 1);

INSERT INTO beispiel04.buildings_address (t_id, t_seq, house_number, street_name, regbl_egid, buildings_prtmntblding_addresses) 
VALUES (102, 0, '26', 'Schänzlistrasse', NULL, 2);


-- Verwaltungsgebäude

INSERT INTO beispiel04.buildings_administrativebuilding (t_id, department, storeys, roof, geometry) 
VALUES (3, 1001, 3, 'terrace', ST_PolygonFromText('POLYGON((607875 228298, 607885 228247, 607928 228254, 607918 228305, 607875 228298))', 21781));


INSERT INTO beispiel04.buildings_address (t_id, t_seq, house_number, street_name, regbl_egid, buildngs_dmstrtvblding_addresses) 
VALUES (103, 0, '4', 'Rötistrasse', 2122818, 3);

INSERT INTO beispiel04.buildings_address (t_id, t_seq, house_number, street_name, regbl_egid, buildngs_dmstrtvblding_addresses) 
VALUES (104, 0, '6', 'Rötistrasse', 9023822, 3);


*/

