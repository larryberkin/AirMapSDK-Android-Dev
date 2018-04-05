package com.airmap.airmapsdk;

import android.test.AndroidTestCase;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.comm.AirMapComm;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=23, manifest = "src/main/AndroidManifest.xml")
public class Tests extends AndroidTestCase {

    @Before
    public void init() {
        AirMap.init(getContext(), false);
        AirMap.enableLogging(true);
    }

    @Test
    public void testAnonLogin() throws InterruptedException {
        AirMap.performAnonymousLogin("vansh", new AirMapCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                assertNull("Got token: " + AirMap.getAuthToken(), response);
            }

            @Override
            public void onError(AirMapException e) {

            }
        });
        Thread.sleep(100000);
    }

    @Test
    public void testComm() throws InterruptedException {
        createTestFlights(5); //Creates 5 flights
        AirMap.getMyFlights(new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(List<AirMapFlight> response) {
                for (AirMapFlight flight : response) {
                    AirMap.startComm(flight.getFlightId(), new AirMapCallback<AirMapComm>() {
                        @Override
                        public void onSuccess(AirMapComm response) {
                            assertNotNull(response);
//                            assertNotNull(response.getExpiresAt());
                            assertNotNull(response.getKey());
//                            assertNotNull(response.getType());
                        }

                        @Override
                        public void onError(AirMapException e) {
                            fail("Error in getting Comm Key");
                        }
                    });
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Error in getting flights Key");
            }
        });
        Thread.sleep(3000);
        deleteAllFlights();
    }

    @Test
    public void testCurrentFlight() throws InterruptedException {
        AirMapFlight flight = new AirMapFlight()
                .setStartsAt(new Date())
                .setEndsAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .setMaxAltitude(15)
                .setCoordinate(new Coordinate(33, 42))
                .setBuffer(150)
                .setNotify(false);
        AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                AirMap.getCurrentFlight(new AirMapCallback<AirMapFlight>() {
                    @Override
                    public void onSuccess(AirMapFlight response) {
                        assertNotNull(response);
                        if (response.isValid()) {
                            assertNotNull(response.getFlightId());
                        }
                    }

                    @Override
                    public void onError(AirMapException e) {
                        fail("Couldn't get current flight");
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {

            }
        });
        Thread.sleep(3000);
        deleteAllFlights();
    }

    @Test
    public void testFetchFlight() throws InterruptedException {
        createTestFlights(3);
        AirMap.getMyFlights(new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(List<AirMapFlight> response) {
                for (final AirMapFlight flight : response) {
                    AirMap.getFlight(flight.getFlightId(), new AirMapCallback<AirMapFlight>() {
                        @Override
                        public void onSuccess(AirMapFlight response) {
                            assertNotNull(response);
                            assertEquals(response.getFlightId(), flight.getFlightId());
                        }

                        @Override
                        public void onError(AirMapException e) {
                            fail("Wasn't able to get flight for ID");
                        }
                    });
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Unable to get all flights");
            }
        });
        Thread.sleep(3000);
        deleteAllFlights();
    }

    @Test
    public void closeFlightTest() throws InterruptedException {
        createTestFlights(1);
        AirMap.getMyFlights(new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(List<AirMapFlight> response) {
                for (final AirMapFlight flight : response) {
                    AirMap.endFlight(flight.getFlightId(), new AirMapCallback<AirMapFlight>() {
                        @Override
                        public void onSuccess(AirMapFlight response) {
                            assertNotNull(response);
                            assertEquals(flight.getFlightId(), response.getFlightId());
                        }

                        @Override
                        public void onError(AirMapException e) {
                            fail("Unable to close flight");
                        }
                    });
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Unable to get all flights");
                assertNotNull(null);
            }
        });
        Thread.sleep(3000);
        deleteAllFlights();
    }

    @Test
    public void listAllFlightsTest() throws InterruptedException {
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
                fail("Could not get all flights");
            }
        });
        Thread.sleep(2500);
    }

    public void createTestFlights(int numFlights) throws InterruptedException {
        for (int i = 0; i < numFlights; i++) {
            AirMapFlight flight = new AirMapFlight()
                    .setStartsAt(new Date(System.currentTimeMillis() + i * 1000))
                    .setMaxAltitude(15 + i)
                    .setCoordinate(new Coordinate(33 + i, 42 + i))
                    .setBuffer(150 + i)
                    .setNotify(false);
            AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
                @Override
                public void onSuccess(AirMapFlight response) {
                    assertNotNull(response);
                }

                @Override
                public void onError(AirMapException e) {
                    e.printStackTrace();
                    fail();
                }
            });
        }
        Thread.sleep(2500);
    }

    @Test
    public void deleteAllFlights() throws InterruptedException {
        AirMap.getMyFlights(new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(List<AirMapFlight> response) {
                for (AirMapFlight flight : response) {
                    AirMap.deleteFlight(flight, new AirMapCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                        }

                        @Override
                        public void onError(AirMapException e) {
                            fail("Failed to delete a flight");
                        }
                    });
                }
            }

            @Override
            public void onError(AirMapException e) {
                fail("Failed to get all flights");
            }
        });
        Thread.sleep(2500);
    }

    @Test
    public void flightPointStatusTest() throws InterruptedException {
        AirMap.checkCoordinate(new Coordinate(31.5, -118), 100d, null, null, true, new Date(), new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                assertNotNull(response);
                if (response.getWeather() != null) {
                    assertNotNull(response.getWeather().getCondition());
                }
            }

            @Override
            public void onError(AirMapException e) {
                System.out.println(e.getMessage());
                fail("Could not get Flight Point Status");
            }
        });
        Thread.sleep(2500);
    }

    @Test
    public void flightPathStatusTest() throws InterruptedException {
        List<Coordinate> coords = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            coords.add(new Coordinate(25, 51 + i));
        }
        AirMap.checkFlightPath(coords, 10, new Coordinate(25, 50), null, null, true, new Date(), new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                assertNotNull(response);
                assertNotNull(response.getAdvisoryColor());
                if (response.getWeather() != null) {
                    assertNotNull(response.getWeather().getCondition());
                }
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
                fail("Could not get Flight Path Status");
            }
        });
        Thread.sleep(2500);
    }

    @Test
    public void flightPolygonStatusTest() throws InterruptedException {
        List<Coordinate> coords = new ArrayList<>(5);
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                coords.add(new Coordinate(25 + i, 50 + j));
        coords.add(new Coordinate(25, 50));
        AirMap.checkPolygon(coords, new Coordinate(25, 50), null, null, true, new Date(), new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                assertNotNull(response);
                assertNotNull(response.getAdvisoryColor());
                if (response.getWeather() != null) {
                    assertNotNull(response.getWeather().getCondition());
                }
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
                fail("Could not get Flight Polygon Status");
            }
        });
        Thread.sleep(25000);
    }

    @Test
    public void createBadFlightTest() throws InterruptedException {
        AirMapFlight flight = new AirMapFlight()
                .setStartsAt(new Date())
                .setMaxAltitude(150000)
                .setCoordinate(new Coordinate(33, 42))
                .setBuffer(3357239)
                .setNotify(false);
        AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                assertNull(response);
                fail("There should have been an error");
            }

            @Override
            public void onError(AirMapException e) {
                //Everything worked as expected
            }
        });
        Thread.sleep(1000);
        deleteAllFlights();
    }
}
