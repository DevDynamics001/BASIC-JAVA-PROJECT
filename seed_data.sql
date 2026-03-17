-- Seed Data for Bus Ticket Booking System

-- 1. Insert Buses
INSERT INTO buses (bus_name, bus_type, total_seats) VALUES ('Morning Star', 'AC Sleeper', 30);
INSERT INTO buses (bus_name, bus_type, total_seats) VALUES ('Orange Tours', 'Non-AC Seater', 40);
INSERT INTO buses (bus_name, bus_type, total_seats) VALUES ('Kaveri Travels', 'Volvo AC', 45);

-- 2. Insert Routes
INSERT INTO routes (source, destination, distance_km, base_fare) VALUES ('Hyderabad', 'Bangalore', 570, 1200.00);
INSERT INTO routes (source, destination, distance_km, base_fare) VALUES ('Chennai', 'Hyderabad', 630, 1500.00);
INSERT INTO routes (source, destination, distance_km, base_fare) VALUES ('Mumbai', 'Pune', 150, 400.00);

-- 3. Insert Route Stops
-- For Hyderabad to Bangalore (Route ID 1)
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (1, 'Hyderabad', 0, 1);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (1, 'Kurnool', 400, 2);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (1, 'Anantapur', 800, 3);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (1, 'Bangalore', 1200, 4);

-- For Chennai to Hyderabad (Route ID 2)
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (2, 'Chennai', 0, 1);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (2, 'Nellore', 500, 2);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (2, 'Ongole', 900, 3);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (2, 'Hyderabad', 1500, 4);

-- For Mumbai to Pune (Route ID 3)
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (3, 'Mumbai', 0, 1);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (3, 'Lonavala', 200, 2);
INSERT INTO route_stops (route_id, stop_name, fare_from_start, stop_order) VALUES (3, 'Pune', 400, 3);

-- 4. Insert Bus Schedules (Dates are set in the future - e.g. 10 days from now)
INSERT INTO bus_schedule (bus_id, route_id, departure_time, arrival_time, travel_date) 
VALUES (1, 1, '20:00', '07:00', SYSDATE + 2);

INSERT INTO bus_schedule (bus_id, route_id, departure_time, arrival_time, travel_date) 
VALUES (2, 2, '21:30', '09:00', SYSDATE + 3);

INSERT INTO bus_schedule (bus_id, route_id, departure_time, arrival_time, travel_date) 
VALUES (3, 3, '06:00', '09:30', SYSDATE + 1);

-- 5. Insert Seats for the buses
-- We will use a PL/SQL block to automatically populate seats for the 3 buses
DECLARE
    v_seats_bus1 NUMBER := 30;
    v_seats_bus2 NUMBER := 40;
    v_seats_bus3 NUMBER := 45;
BEGIN
    -- Populate seats for Bus 1 (Morning Star - 30 seats)
    FOR i IN 1..v_seats_bus1 LOOP
        INSERT INTO seats (bus_id, seat_number, seat_status) VALUES (1, i, 'AVAILABLE');
    END LOOP;
    
    -- Populate seats for Bus 2 (Orange Tours - 40 seats)
    FOR i IN 1..v_seats_bus2 LOOP
        INSERT INTO seats (bus_id, seat_number, seat_status) VALUES (2, i, 'AVAILABLE');
    END LOOP;
    
    -- Populate seats for Bus 3 (Kaveri Travels - 45 seats)
    FOR i IN 1..v_seats_bus3 LOOP
        INSERT INTO seats (bus_id, seat_number, seat_status) VALUES (3, i, 'AVAILABLE');
    END LOOP;
END;
/

COMMIT;
