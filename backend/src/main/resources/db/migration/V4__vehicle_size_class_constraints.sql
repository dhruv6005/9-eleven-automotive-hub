DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM service_prices
        WHERE vehicle_size_class NOT IN ('SMALL', 'MEDIUM', 'LARGE', 'SUV')
    ) THEN
        ALTER TABLE service_prices ADD CONSTRAINT ck_service_prices_vehicle_size_class
            CHECK (vehicle_size_class IN ('SMALL', 'MEDIUM', 'LARGE', 'SUV'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM vehicles
        WHERE size_class IS NOT NULL AND size_class NOT IN ('SMALL', 'MEDIUM', 'LARGE', 'SUV')
    ) THEN
        ALTER TABLE vehicles ADD CONSTRAINT ck_vehicles_size_class
            CHECK (size_class IS NULL OR size_class IN ('SMALL', 'MEDIUM', 'LARGE', 'SUV'));
    END IF;
END $$;
