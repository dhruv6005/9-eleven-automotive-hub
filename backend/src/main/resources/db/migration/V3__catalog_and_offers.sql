ALTER TABLE service_categories ADD COLUMN slug VARCHAR(140);
ALTER TABLE service_categories ADD COLUMN image_url TEXT;
ALTER TABLE service_categories ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;
UPDATE service_categories SET sort_order = display_order;
ALTER TABLE service_categories DROP COLUMN display_order;
ALTER TABLE service_categories ALTER COLUMN slug SET NOT NULL;
CREATE UNIQUE INDEX ux_service_categories_slug ON service_categories (LOWER(slug)) WHERE deleted_at IS NULL;

ALTER TABLE services ADD COLUMN slug VARCHAR(160);
ALTER TABLE services ADD COLUMN image_url TEXT;
ALTER TABLE services ADD COLUMN short_benefit VARCHAR(300);
ALTER TABLE services ALTER COLUMN slug SET NOT NULL;
CREATE UNIQUE INDEX ux_services_slug ON services (LOWER(slug)) WHERE deleted_at IS NULL;

-- Pricing rows inherit the audited entity base class and retain its soft-delete metadata.
ALTER TABLE service_prices ADD COLUMN deleted_at TIMESTAMPTZ;

INSERT INTO service_categories (id,name,slug,description,sort_order,active) VALUES
('30000000-0000-0000-0000-000000000001','Detailing','detailing','Interior and exterior care to restore and protect your vehicle.',10,TRUE),
('30000000-0000-0000-0000-000000000002','Paint and Body','paint-and-body','Cosmetic repairs and refinishing for vehicle bodywork.',20,TRUE),
('30000000-0000-0000-0000-000000000003','PPF','ppf','Paint protection film installation for high-impact areas and panels.',30,TRUE),
('30000000-0000-0000-0000-000000000004','Coatings','coatings','Protective surface coatings for paint, glass and wheels.',40,TRUE),
('30000000-0000-0000-0000-000000000005','Mechanical','mechanical','Routine maintenance and mechanical repair services.',50,TRUE),
('30000000-0000-0000-0000-000000000006','Garage and Electrical','garage-and-electrical','Workshop diagnostics, electrical and garage services.',60,TRUE),
('30000000-0000-0000-0000-000000000007','Accessories','accessories','Vehicle accessories supplied and fitted by the workshop.',70,TRUE),
('30000000-0000-0000-0000-000000000008','Add-ons','add-ons','Finishing enhancements that complement your service.',80,TRUE);

INSERT INTO services (id,category_id,name,slug,description,short_benefit,active) VALUES
('31000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','Exterior Wash and Wax','exterior-wash-and-wax','Hand wash, dry and wax finish for exterior surfaces.','A clean finish with added gloss.',TRUE),
('31000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000001','Interior Deep Cleaning','interior-deep-cleaning','Deep cleaning of cabin surfaces, carpets and upholstery.','Refreshes the cabin and its touchpoints.',TRUE),
('31000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000001','Full Vehicle Detailing','full-vehicle-detailing','Detailed interior and exterior cleaning with careful finishing.','A thorough refresh inside and out.',TRUE),
('31000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000002','Dent Repair','dent-repair','Assessment and repair of eligible dents and panel damage.','Restore panel shape with a careful repair.',TRUE),
('31000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000002','Panel Repainting','panel-repainting','Preparation and repainting of a selected body panel.','Refresh damaged or faded paintwork.',TRUE),
('31000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000002','Bumper Repair','bumper-repair','Repair and refinishing for common bumper scuffs and damage.','Improve the finish of bumper damage.',TRUE),
('31000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000003','Front Bumper PPF','front-bumper-ppf','Paint protection film fitted to the front bumper.','Helps guard against road debris.',TRUE),
('31000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000003','Bonnet PPF','bonnet-ppf','Paint protection film fitted to the bonnet.','Adds a clear protective layer.',TRUE),
('31000000-0000-0000-0000-000000000009','30000000-0000-0000-0000-000000000003','Full Body PPF','full-body-ppf','Paint protection film coverage planned for the vehicle body.','Broad paint coverage tailored to the vehicle.',TRUE),
('31000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000004','Ceramic Paint Coating','ceramic-paint-coating','Ceramic coating applied to prepared painted surfaces.','Supports gloss and easier routine cleaning.',TRUE),
('31000000-0000-0000-0000-000000000011','30000000-0000-0000-0000-000000000004','Glass Coating','glass-coating','Protective coating for selected exterior glass surfaces.','Helps water move off treated glass.',TRUE),
('31000000-0000-0000-0000-000000000012','30000000-0000-0000-0000-000000000004','Wheel Coating','wheel-coating','Protective coating applied to cleaned wheel surfaces.','Makes routine wheel cleaning easier.',TRUE),
('31000000-0000-0000-0000-000000000013','30000000-0000-0000-0000-000000000005','Periodic Maintenance','periodic-maintenance','Scheduled inspection and maintenance based on vehicle needs.','Keep routine upkeep organised.',TRUE),
('31000000-0000-0000-0000-000000000014','30000000-0000-0000-0000-000000000005','Brake Inspection and Service','brake-inspection-and-service','Inspection and servicing of brake components as required.','Review braking components for wear.',TRUE),
('31000000-0000-0000-0000-000000000015','30000000-0000-0000-0000-000000000005','Suspension Inspection','suspension-inspection','Inspection of suspension components and reported concerns.','Assess handling and suspension concerns.',TRUE),
('31000000-0000-0000-0000-000000000016','30000000-0000-0000-0000-000000000006','Engine Diagnostics','engine-diagnostics','Diagnostic scan and initial assessment of engine warnings.','Identify diagnostic trouble codes.',TRUE),
('31000000-0000-0000-0000-000000000017','30000000-0000-0000-0000-000000000006','Battery and Charging Check','battery-and-charging-check','Check battery condition and charging system operation.','Review starting and charging health.',TRUE),
('31000000-0000-0000-0000-000000000018','30000000-0000-0000-0000-000000000006','Electrical Fault Diagnosis','electrical-fault-diagnosis','Trace and assess reported vehicle electrical faults.','Narrow down electrical issues.',TRUE),
('31000000-0000-0000-0000-000000000019','30000000-0000-0000-0000-000000000007','Dash Camera Installation','dash-camera-installation','Fitment and setup of a customer-selected dash camera.','Neat installation and basic setup.',TRUE),
('31000000-0000-0000-0000-000000000020','30000000-0000-0000-0000-000000000007','Parking Sensor Installation','parking-sensor-installation','Fitment of compatible parking sensors.','Add assistance when manoeuvring.',TRUE),
('31000000-0000-0000-0000-000000000021','30000000-0000-0000-0000-000000000007','Infotainment Upgrade','infotainment-upgrade','Installation assessment and fitment of compatible infotainment equipment.','Modernise the cabin interface.',TRUE),
('31000000-0000-0000-0000-000000000022','30000000-0000-0000-0000-000000000008','Headlight Restoration','headlight-restoration','Clean and restore eligible hazy headlight lenses.','Improve lens clarity and appearance.',TRUE),
('31000000-0000-0000-0000-000000000023','30000000-0000-0000-0000-000000000008','Odour Treatment','odour-treatment','Targeted cabin odour treatment following inspection.','Freshen the cabin environment.',TRUE),
('31000000-0000-0000-0000-000000000024','30000000-0000-0000-0000-000000000008','Engine Bay Cleaning','engine-bay-cleaning','Careful cleaning of accessible engine bay surfaces.','Tidy accessible engine bay surfaces.',TRUE);
