-- Disable foreign key checks for dropping tables (optional cleanup)
-- DROP TABLE ticket_passengers CASCADE CONSTRAINTS;
-- DROP TABLE payments CASCADE CONSTRAINTS;
-- DROP TABLE bookings CASCADE CONSTRAINTS;
-- DROP TABLE seats CASCADE CONSTRAINTS;
-- DROP TABLE bus_schedule CASCADE CONSTRAINTS;
-- DROP TABLE route_stops CASCADE CONSTRAINTS;
-- DROP TABLE routes CASCADE CONSTRAINTS;
-- DROP TABLE buses CASCADE CONSTRAINTS;
-- DROP TABLE users CASCADE CONSTRAINTS;
-- DROP TABLE admin CASCADE CONSTRAINTS;

-- 1. admin
CREATE TABLE admin (
    username VARCHAR2(50) PRIMARY KEY,
    password VARCHAR2(100) NOT NULL
);

INSERT INTO admin (username, password) VALUES ('admin', 'admin123'); -- Default admin

-- 2. users
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    password VARCHAR2(100) NOT NULL,
    phone VARCHAR2(15) NOT NULL
);
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER users_trig
BEFORE INSERT ON users FOR EACH ROW
BEGIN
    IF :NEW.user_id IS NULL THEN
        SELECT users_seq.NEXTVAL INTO :NEW.user_id FROM dual;
    END IF;
END;
/

-- 3. buses
CREATE TABLE buses (
    bus_id NUMBER PRIMARY KEY,
    bus_name VARCHAR2(100) NOT NULL,
    bus_type VARCHAR2(50),
    total_seats NUMBER NOT NULL
);
CREATE SEQUENCE buses_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER buses_trig
BEFORE INSERT ON buses FOR EACH ROW
BEGIN
    IF :NEW.bus_id IS NULL THEN
        SELECT buses_seq.NEXTVAL INTO :NEW.bus_id FROM dual;
    END IF;
END;
/

-- 4. routes
CREATE TABLE routes (
    route_id NUMBER PRIMARY KEY,
    source VARCHAR2(100) NOT NULL,
    destination VARCHAR2(100) NOT NULL,
    distance_km NUMBER,
    base_fare NUMBER(10, 2) NOT NULL
);
CREATE SEQUENCE routes_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER routes_trig
BEFORE INSERT ON routes FOR EACH ROW
BEGIN
    IF :NEW.route_id IS NULL THEN
        SELECT routes_seq.NEXTVAL INTO :NEW.route_id FROM dual;
    END IF;
END;
/

-- 5. route_stops
CREATE TABLE route_stops (
    route_id NUMBER REFERENCES routes(route_id) ON DELETE CASCADE,
    stop_name VARCHAR2(100) NOT NULL,
    fare_from_start NUMBER(10, 2) NOT NULL,
    stop_order NUMBER NOT NULL,
    PRIMARY KEY (route_id, stop_order)
);

-- 6. bus_schedule
CREATE TABLE bus_schedule (
    schedule_id NUMBER PRIMARY KEY,
    bus_id NUMBER REFERENCES buses(bus_id) ON DELETE CASCADE,
    route_id NUMBER REFERENCES routes(route_id) ON DELETE CASCADE,
    departure_time VARCHAR2(20) NOT NULL,
    arrival_time VARCHAR2(20) NOT NULL,
    travel_date DATE NOT NULL
);
CREATE SEQUENCE schedule_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER schedule_trig
BEFORE INSERT ON bus_schedule FOR EACH ROW
BEGIN
    IF :NEW.schedule_id IS NULL THEN
        SELECT schedule_seq.NEXTVAL INTO :NEW.schedule_id FROM dual;
    END IF;
END;
/

-- 7. seats
CREATE TABLE seats (
    bus_id NUMBER REFERENCES buses(bus_id) ON DELETE CASCADE,
    seat_number NUMBER NOT NULL,
    seat_status VARCHAR2(20) DEFAULT 'AVAILABLE',
    PRIMARY KEY (bus_id, seat_number)
);

-- 8. bookings
CREATE TABLE bookings (
    booking_id NUMBER PRIMARY KEY,
    user_id NUMBER REFERENCES users(user_id) ON DELETE CASCADE,
    schedule_id NUMBER REFERENCES bus_schedule(schedule_id) ON DELETE CASCADE,
    seat_number NUMBER,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    boarding_stop VARCHAR2(100),
    dropping_stop VARCHAR2(100),
    total_fare NUMBER(10, 2),
    payment_done NUMBER(1) DEFAULT 0
);
CREATE SEQUENCE bookings_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER bookings_trig
BEFORE INSERT ON bookings FOR EACH ROW
BEGIN
    IF :NEW.booking_id IS NULL THEN
        SELECT bookings_seq.NEXTVAL INTO :NEW.booking_id FROM dual;
    END IF;
END;
/

-- 9. ticket_passengers
CREATE TABLE ticket_passengers (
    ticket_id VARCHAR2(50) NOT NULL,
    booking_id NUMBER REFERENCES bookings(booking_id) ON DELETE CASCADE,
    passenger_name VARCHAR2(100) NOT NULL,
    age NUMBER NOT NULL,
    gender VARCHAR2(10) NOT NULL,
    phone VARCHAR2(15),
    seat_number NUMBER NOT NULL,
    fare NUMBER(10, 2) NOT NULL,
    PRIMARY KEY (ticket_id, seat_number)
);

-- 10. payments
CREATE TABLE payments (
    payment_id NUMBER PRIMARY KEY,
    booking_id NUMBER REFERENCES bookings(booking_id) ON DELETE CASCADE,
    amount NUMBER(10, 2) NOT NULL,
    payment_mode VARCHAR2(50) NOT NULL,
    payment_status VARCHAR2(20) DEFAULT 'SUCCESS'
);
CREATE SEQUENCE payments_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER payments_trig
BEFORE INSERT ON payments FOR EACH ROW
BEGIN
    IF :NEW.payment_id IS NULL THEN
        SELECT payments_seq.NEXTVAL INTO :NEW.payment_id FROM dual;
    END IF;
END;
/

COMMIT;
