package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import bus_ticket.DBconnection;

/** Handles CRUD operations for routes and route stops. */
public class RouteDAO {

    public void addRoute(String source, String destination, int distance, double fare) {
        if (source == null || source.trim().isEmpty()) {
            System.out.println("  Source cannot be empty!"); return;
        }
        if (destination == null || destination.trim().isEmpty()) {
            System.out.println("  Destination cannot be empty!"); return;
        }
        if (distance <= 0) {
            System.out.println("  Distance must be greater than 0!"); return;
        }
        if (fare <= 0) {
            System.out.println("  Base fare must be greater than 0!"); return;
        }

        String sql = "INSERT INTO routes (source, destination, distance_km, base_fare) VALUES (?, ?, ?, ?)";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, source.trim());
            ps.setString(2, destination.trim());
            ps.setInt(3, distance);
            ps.setDouble(4, fare);
            ps.executeUpdate();
            System.out.println("  Route added successfully.");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not add route.");
        }
    }

    public void deleteRoute(int routeId) {
        String checkSchedule = "SELECT COUNT(*) AS cnt FROM bus_schedule WHERE route_id=?";
        String deleteStops   = "DELETE FROM route_stops WHERE route_id=?";
        String deleteRoute   = "DELETE FROM routes WHERE route_id=?";

        try (Connection con = DBconnection.getConnection()) {
            PreparedStatement psCheck = con.prepareStatement(checkSchedule);
            psCheck.setInt(1, routeId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt("cnt") > 0) {
                System.out.println("  Cannot delete! Route " + routeId + " is used in active schedules."); return;
            }
            PreparedStatement psDel1 = con.prepareStatement(deleteStops);
            psDel1.setInt(1, routeId);
            psDel1.executeUpdate();

            PreparedStatement psDel2 = con.prepareStatement(deleteRoute);
            psDel2.setInt(1, routeId);
            int rows = psDel2.executeUpdate();
            System.out.println(rows > 0
                    ? "  Route " + routeId + " deleted successfully."
                    : "  Route ID " + routeId + " not found.");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not delete route.");
        }
    }

    public void viewRoutes() {
        String sql = "SELECT route_id, source, destination, distance_km, base_fare FROM routes ORDER BY route_id";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n  ╔══════════════════════════════════════════════════╗");
            System.out.println("  ║           AVAILABLE ROUTES & FARES              ║");
            System.out.println("  ╚══════════════════════════════════════════════════╝");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int routeId = rs.getInt("route_id");
                System.out.println("\n  Route ID  : " + routeId);
                System.out.println("  From      : " + rs.getString("source"));
                System.out.println("  To        : " + rs.getString("destination"));
                System.out.println("  Distance  : " + rs.getInt("distance_km") + " KM");
                System.out.printf ("  Full Fare : Rs.%.2f%n", rs.getDouble("base_fare"));
                System.out.println("  -- Stops --");
                viewStopsForRoute(con, routeId);
                System.out.println("  --------------------------------------------------");
            }
            if (!found) System.out.println("  No routes found.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch routes.");
        }
    }

    private void viewStopsForRoute(Connection con, int routeId) {
        String sql = "SELECT stop_order, stop_name, fare_from_start FROM route_stops WHERE route_id=? ORDER BY stop_order";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ResultSet rs = ps.executeQuery();
            System.out.printf("  %-5s %-22s %-15s%n", "Stop#", "Stop Name", "Fare from Start");
            while (rs.next()) {
                double fare = rs.getDouble("fare_from_start");
                System.out.printf("  %-5d %-22s %-15s%n",
                        rs.getInt("stop_order"),
                        rs.getString("stop_name"),
                        fare == 0 ? "Starting Point" : String.format("Rs.%.2f", fare));
            }
        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch stops.");
        }
    }

    /** Returns list of [stopName, fareFromStart, stopOrder] for a route. */
    public List<String[]> getStops(int routeId) {
        List<String[]> stops = new ArrayList<>();
        String sql = "SELECT stop_name, fare_from_start, stop_order FROM route_stops WHERE route_id=? ORDER BY stop_order";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stops.add(new String[]{
                    rs.getString("stop_name"),
                    String.valueOf(rs.getDouble("fare_from_start")),
                    String.valueOf(rs.getInt("stop_order"))
                });
            }
        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch stops.");
        }
        return stops;
    }

    /** Calculates fare between two stop names on a route. */
    public double calculateFare(int routeId, String boardingStop, String droppingStop) {
        double boardingFare = 0, droppingFare = 0;
        String sql = "SELECT stop_name, fare_from_start FROM route_stops WHERE route_id=?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("stop_name");
                double fare = rs.getDouble("fare_from_start");
                if (name.equalsIgnoreCase(boardingStop)) boardingFare = fare;
                if (name.equalsIgnoreCase(droppingStop)) droppingFare = fare;
            }
        } catch (Exception e) {
            System.out.println("  ERROR: Could not calculate fare.");
        }
        return droppingFare - boardingFare;
    }

    /** Returns the route_id for a given schedule, or -1 if not found. */
    public int getRouteIdForSchedule(int scheduleId) {
        String sql = "SELECT route_id FROM bus_schedule WHERE schedule_id=?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("route_id");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch route for schedule.");
        }
        return -1;
    }
}