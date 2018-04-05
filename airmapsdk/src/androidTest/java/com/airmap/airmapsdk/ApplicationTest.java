package com.airmap.airmapsdk;


import android.app.Application;
import android.test.ApplicationTestCase;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraft;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraftManufacturer;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraftModel;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.networking.services.TelemetryService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL3Nzby5haXJtYXAuaW8vIiwic3ViIjoiYXV0aDB8NTc2MWE0Mjc5NzMyZjU4NDRiMWRiODQ0IiwiYXVkIjoiMmlWMVhTZmRMSk5PZlppVFo5Skdkck5IdGNOellzdHQiLCJleHAiOjE0NzY0MzAzMTEsImlhdCI6MTQ3NjM5NDMxMX0.TurplRL1cmkXmLcvPQYuTMDqe8Ck-PWTW8QBB5YoX3Y";
        AirMap.init(getSystemContext(), token);
        AirMap.enableLogging(true);
    }

    public void testCreateFlight() throws InterruptedException {
        final Date now = new Date();
        final AirMapFlight flight = new AirMapFlight()
                .setStartsAt(now)
                .setMaxAltitude(15)
                .setCoordinate(new Coordinate(33, 42))
                .setBuffer(150)
                .setNotify(false);
        AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                assertNotNull(response);
                assertEquals(response.getBuffer(), 150, 0.01);
                assertEquals(response.getStartsAt(), now);
                assertEquals(response.getMaxAltitude(), 15, 0.01);
                assertEquals(response.getCoordinate(), new Coordinate(33, 42));
                assertFalse(response.shouldNotify());
                AirMap.endFlight(flight.getFlightId(), null);
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
                fail();
            }
        });
        Thread.sleep(2000);
    }

    public void testValidCredentials() {
        assertTrue(AirMap.hasValidCredentials());
    }

    public void testInitialized() {
        assertTrue(AirMap.hasBeenInitialized());
    }

    public void testCertPinning() {
        AirMap.enableCertificatePinning(true);
        assertTrue(AirMap.isCertificatePinningEnabled());
        AirMap.enableCertificatePinning(false);
        assertFalse(AirMap.isCertificatePinningEnabled());
    }

    public void testGetManufacturers() throws InterruptedException {
        AirMap.getManufacturers(new AirMapCallback<List<AirMapAircraftManufacturer>>() {
            @Override
            public void onSuccess(List<AirMapAircraftManufacturer> response) {
                assertNotNull(response);
                assertTrue(response.size() != 0);
                for (AirMapAircraftManufacturer manufacturer : response) {
                    assertNotNull(manufacturer);
                    assertNotNull(manufacturer.getId());
                    assertNotNull(manufacturer.getName());
                }
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
                fail("Error fetching manufacturers");
            }
        });
        Thread.sleep(2000);
    }

    public void testGetModels() throws InterruptedException {
        AirMap.getModels(new AirMapCallback<List<AirMapAircraftModel>>() {
            @Override
            public void onSuccess(List<AirMapAircraftModel> response) {
                assertNotNull(response);
                assertTrue(response.size() != 0);
                for (AirMapAircraftModel model : response) {
                    assertNotNull(model.getManufacturer());
                    assertNotNull(model.getName());
                    assertNotNull(model.getMetaData());
                    assertNotNull(model.getModelId());
                }
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
                fail("Error fetching all drone models");
            }
        });
        Thread.sleep(2000);
    }

    public void testGetModel() throws InterruptedException {
        final String modelIdToGet = "0bdc3e35-75ba-4e02-9040-d336d11f5202";
        AirMap.getModel(modelIdToGet, new AirMapCallback<AirMapAircraftModel>() {
            @Override
            public void onSuccess(AirMapAircraftModel response) {
                assertNotNull(response);
                assertEquals(modelIdToGet, response.getModelId());
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error getting specific model");
            }
        });
        Thread.sleep(1000);
    }

    public void testGetAllFlights() throws InterruptedException {
        AirMap.getMyFlights(new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(List<AirMapFlight> response) {
                assertNotNull(response);
                for (AirMapFlight flight : response) {
                    assertNotNull(flight);
                    assertNotNull(flight.getFlightId());
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error getting all pilot flights");
            }
        });
        Thread.sleep(3500);
    }

    public void testEndFlight() throws InterruptedException {
        final AirMapFlight flightToCreate = new AirMapFlight().setCoordinate(new Coordinate(0, 0)).setBuffer(10).setMaxAltitude(15).setNotify(false).setPublic(false);
        AirMap.createFlight(flightToCreate, new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(final AirMapFlight createdFlight) {
                assertNotNull(createdFlight);
                assertNotNull(createdFlight.getFlightId());
                AirMap.endFlight(createdFlight.getFlightId(), new AirMapCallback<AirMapFlight>() {
                    @Override
                    public void onSuccess(AirMapFlight endedFlight) {
                        assertNotNull(endedFlight);
                    }

                    @Override
                    public void onError(AirMapException e) {
                        fail("Error ending flight " + e.getDetailedMessage());
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error creating the flight to be ended");
            }
        });
        Thread.sleep(5000);
    }

    public void testGetPilot() throws InterruptedException {
        AirMap.getPilot(new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(AirMapPilot response) {
                assertNotNull(response);
                assertNotNull(response.getPilotId());
                assertNotNull(response.getAppMetaData());
                assertNotNull(response.getUserMetaData());
                assertTrue(response.getEmail().contains("@"));
                assertEquals(response.getPilotId(), AirMap.getUserId());
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error getting pilot: " + e.getMessage());
            }
        });
        Thread.sleep(1000);
    }

    public void testGetPilotPermits() throws InterruptedException {
        AirMap.getAuthenticatedPilotPermits(new AirMapCallback<List<AirMapPilotPermit>>() {
            @Override
            public void onSuccess(List<AirMapPilotPermit> response) {
                assertNotNull(response);
                for (AirMapPilotPermit permit :
                        response) {
                    assertNotNull(permit);
                    assertNotNull(permit.getApplicationId());
                }
            }

            @Override
            public void onError(AirMapException e) {

            }
        });
        Thread.sleep(1000);
    }

    public void testUpdatePilot() throws InterruptedException {
        AirMap.getPilot(new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(AirMapPilot response) {
                assertNotNull(response);
                response.setLastName("Doe");
                AirMap.updatePilot(response, new AirMapCallback<AirMapPilot>() {
                    @Override
                    public void onSuccess(AirMapPilot response) {
                        assertNotNull(response);
                        assertEquals(response.getLastName(), "Doe");
                    }

                    @Override
                    public void onError(AirMapException e) {
                        fail("Error updating pilot " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error getting pilot before updating " + e.getMessage());
            }
        });
        Thread.sleep(5000);
    }

    public void testUpdatePilot2() throws InterruptedException {
        AirMapPilot pilot = new AirMapPilot().setLastName("Gandhi");
        AirMap.updatePilot(pilot, new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(AirMapPilot response) {
                assertNotNull(response);
                assertEquals(response.getLastName(), "Gandhi");
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error updating pilot " + e.getMessage());
            }
        });
        Thread.sleep(4000);
    }

    //This method assumes you have some aircraft in your account
    public void testGetAircraft() throws InterruptedException {
        AirMap.getAircraft(new AirMapCallback<List<AirMapAircraft>>() {
            @Override
            public void onSuccess(List<AirMapAircraft> response) {
                assertNotNull(response);
                for (AirMapAircraft aircraft : response) {
                    assertNotNull(aircraft);
                    assertNotNull(aircraft.getAircraftId());
                    assertNotNull(aircraft.getNickname());
                    assertNotNull(aircraft.getModel());
                    assertNotNull(aircraft.getModel().getModelId());
                    assertNotNull(aircraft.getModel().getMetaData());
                    assertNotNull(aircraft.getModel().getManufacturer());
                    assertNotNull(aircraft.getModel().getName());
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error getting all user's aircraft " + e.getMessage());
            }
        });
        Thread.sleep(2000);
    }

//    public void testGetAircraftById() throws InterruptedException {
//        String id = ";
//        AirMap.getAircraft(id, new AirMapCallback<AirMapAircraft>() {
//            @Override
//            public void onSuccess(AirMapAircraft response) {
//                assertNotNull(response);
//                assertNotNull(response.getAircraftId());
//                assertNotNull(response.getNickname());
//                assertNotNull(response.getModel());
//                assertNotNull(response.getModel().getModelId());
//                assertNotNull(response.getModel().getMetaData());
//                assertNotNull(response.getModel().getManufacturer());
//                assertNotNull(response.getModel().getName());
//            }
//
//            @Override
//            public void onError(AirMapException e) {
//                fail("Error getting aircraft by id");
//            }
//        });
//        Thread.sleep(1000);
//    }

    public void testTileSourceUrl() {
        assertNotNull(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Dark));
        assertNotNull(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Light));
        assertNotNull(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Satellite));
        assertNotNull(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Standard));

        assertTrue(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Dark).contains("tilejson"));
        assertTrue(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Light).contains("tilejson"));
        assertTrue(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Satellite).contains("tilejson"));
        assertTrue(AirMap.getTileSourceUrl(null, MappingService.AirMapMapTheme.Standard).contains("tilejson"));

        List<MappingService.AirMapLayerType> layers = Arrays.asList(MappingService.AirMapLayerType.values()); //Adds all layers

        assertNotNull(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Dark));
        assertNotNull(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Light));
        assertNotNull(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Satellite));
        assertNotNull(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Standard));

        assertTrue(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Dark).contains("tilejson"));
        assertTrue(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Light).contains("tilejson"));
        assertTrue(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Satellite).contains("tilejson"));
        assertTrue(AirMap.getTileSourceUrl(layers, MappingService.AirMapMapTheme.Standard).contains("tilejson"));
    }

    public void testCreateAircraft() throws InterruptedException {
        final AirMapAircraft aircraft = new AirMapAircraft().setNickname("Test").setModel(new AirMapAircraftModel().setModelId("0bdc3e35-75ba-4e02-9040-d336d11f5202")); //Phantom 1
        AirMap.createAircraft(aircraft, new AirMapCallback<AirMapAircraft>() {
            @Override
            public void onSuccess(AirMapAircraft response) {
                assertNotNull(response);
                assertEquals(response.getNickname(), "Test");
                assertEquals(response.getModel(), aircraft.getModel());
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error creating aircraft " + e.getMessage());
            }
        });
        Thread.sleep(2000);
    }

    public void testCheckCoordinate() throws InterruptedException {
        AirMap.checkCoordinate(new Coordinate(34, -118), 1000d, Arrays.asList(MappingService.AirMapAirspaceType.values()), null, false, null, new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                assertNotNull(response);
                assertNull(response.getWeather());
                for (AirMapStatusAdvisory advisory : response.getAdvisories()) {
                    assertNotNull(advisory);
                    if (advisory.getType() != MappingService.AirMapAirspaceType.PowerPlant) {
                        assertNull(advisory.getPowerPlantProperties());
                    }
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error checking coordinate " + e.getMessage());
            }
        });
        Thread.sleep(5000);
    }

    public void testWeather() throws InterruptedException {
        AirMap.checkCoordinate(new Coordinate(34, -118), null, null, null, true, null, new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                assertNotNull(response);
                assertNotNull(response.getWeather());
                assertTrue(response.getWeather().isValid());
                assertNotNull(response.getWeather().getIcon());
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error getting weather");
            }
        });
        Thread.sleep(2000);
    }

    public void testTelemetry() throws InterruptedException {
        final AirMapFlight flight = new AirMapFlight()
                .setCoordinate(new Coordinate(31.5, -118))
                .setStartsAt(new Date())
                .setPublic(true)
                .setMaxAltitude(100)
                .setBuffer(500)
                .setNotify(true);
        AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                assertNotNull(response);
                TelemetryService service = new TelemetryService(response);
                assertNotNull(service);
                assertTrue(service.getFlight().equals(response));
                service.sendMessage(response.getCoordinate(), 10, 15, 20, 1000f);
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(2000);
    }

//    public void testTrafficService() throws InterruptedException {
//        AirMap.enableLogging(true);
//        AirMap.enableTrafficAlerts(new AirMapTrafficListener() {
//            @Override
//            public void onAddTraffic(List<AirMapTraffic> added) {
//
//            }
//
//            @Override
//            public void onUpdateTraffic(List<AirMapTraffic> updated) {
//
//            }
//
//            @Override
//            public void onRemoveTraffic(List<AirMapTraffic> removed) {
//
//            }
//        });
//        Thread.sleep(600000);
//    }
}